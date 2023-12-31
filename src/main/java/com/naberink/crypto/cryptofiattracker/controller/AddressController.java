package com.naberink.crypto.cryptofiattracker.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.time.ZoneId;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


import com.naberink.crypto.cryptofiattracker.dto.DailyDatasAndYoYs;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.naberink.crypto.cryptofiattracker.Util;
import com.naberink.crypto.cryptofiattracker.blockstream.BlockStreamApi;
import com.naberink.crypto.cryptofiattracker.blockstream.response.Transaction;
import com.naberink.crypto.cryptofiattracker.dto.DailyData;
import com.naberink.crypto.cryptofiattracker.dto.MappedTransaction;
import com.naberink.crypto.cryptofiattracker.dto.YearOverYearData;
import com.naberink.crypto.cryptofiattracker.mapping.TransactionMapper;
import com.naberink.crypto.cryptofiattracker.service.PricesService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AddressController {

    private final PricesService pricesService;
    private final BlockStreamApi blockStreamApi;

    @GetMapping("/transactions/{addresses}")
    public Response transactions(@PathVariable("addresses") String addresses) {
        log.info("Fetching data for address: {}", addresses);

        Set<String> splittedAddresses = Set.of(addresses.split(","));
        // Removed explicit System.out.println statements, consider using a logger
        // instead

        var priceMap = pricesService.getPriceMap();
        // Logging size of pricemap, consider using logger.debug if necessary

        // use treeset so we have old to new AND avoid duplicates
        List<Transaction> transactions = getTransactions(splittedAddresses);

        if (transactions.isEmpty()) {
            return null; // Consider returning a response indicating no transactions
        }
        var mappedTransactions = TransactionMapper.mapTransactions(transactions, splittedAddresses, priceMap);
        var dailyDatasAndYoYs = calculateDailyDataListAndYoY(mappedTransactions, priceMap);

        MappedTransaction last = mappedTransactions.getLast();
        BigDecimal profit = last.getTotalAmountEurValueNow().subtract(last.getTotalAmountEurAtMomentOfDepositing());
        BigDecimal percentProfit = profit.divide(last.getTotalAmountEurAtMomentOfDepositing(), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);



        return new Response(dailyDatasAndYoYs.getDailyDataList(), mappedTransactions, dailyDatasAndYoYs.getYearOverYearDataList(), last.getTotalAmountEurAtMomentOfDepositing(),
                last.getTotalAmountEurValueNow(), last.getTotalSatsBalance(), percentProfit, priceMap.lastEntry().getValue().setScale(2, RoundingMode.HALF_UP), Instant.ofEpochMilli(priceMap.lastEntry().getKey()));
    }

    private List<Transaction> getTransactions(Set<String> addresses) {
        // make a flatmap that returns all transactionss combined

        return addresses.stream().map(address -> {
            try {
                return blockStreamApi.getTransactionsForAddress(address);
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt(); // Proper handling of InterruptedException
                throw new RuntimeException("Error fetching transactions for address: " + address, e);
            }
        }).flatMap(list -> list.stream())
            .distinct().sorted(Comparator.comparingLong(tx ->tx.getStatus().getBlockTime())).toList();
    }

    private DailyDatasAndYoYs calculateDailyDataListAndYoY(List<MappedTransaction> mappedTransactions,
                                                           NavigableMap<Long, BigDecimal> priceMap) {
        List<DailyData> dailyDataList = new ArrayList<>();

        List<YearOverYearData> yearOverYearDataList = new ArrayList<>();

        if (mappedTransactions.isEmpty()) {
            return new DailyDatasAndYoYs(dailyDataList, null); // Return an empty list if there are no transactions
        }

        long firstTransactionTime = mappedTransactions.get(0).getTimestamp().truncatedTo(ChronoUnit.DAYS)
                .toEpochMilli();
        NavigableMap<Long, BigDecimal> relevantPrices = priceMap.tailMap(firstTransactionTime, true);

        BigDecimal totalEurDeposited = BigDecimal.ZERO;
        long totalSats = 0;
        for (Map.Entry<Long, BigDecimal> entry : relevantPrices.entrySet()) {
            Instant day = Instant.ofEpochMilli(entry.getKey());
            BigDecimal price = entry.getValue();


            for (MappedTransaction transaction : mappedTransactions) {
                // Include transaction only if it occurred on the same day
                if (transaction.getTimestamp().truncatedTo(ChronoUnit.DAYS).equals(day.truncatedTo(ChronoUnit.DAYS))) {
                    totalSats += transaction.getTxAmountSats();
                    totalEurDeposited = totalEurDeposited.add(transaction.getTxAmountEur());
                }
            }
            

            BigDecimal totalEurValueNow = new BigDecimal(totalSats)
                    .multiply(price)
                    .divide(Util.SATS_IN_BITCOIN)
                    .setScale(2, RoundingMode.HALF_UP);

            DailyData dailyData = new DailyData();
            dailyData.setDay(day);
            dailyData.setTotalSatsBalance(totalSats);
            dailyData.setTotalAmountEurValueNow(totalEurValueNow);
            dailyData.setTotalAmountEurAtMomentOfDepositing(totalEurDeposited);


            dailyDataList.add(dailyData);


            LocalDate localDate = day.atZone(ZoneId.systemDefault()).toLocalDate();
            // only execute IF 31 december XOR last entry, because if it's 31 dec there might be 2 entries, so we only hop into the if it's 31 december AND NOT last entry.
            if (localDate.getMonth() == Month.DECEMBER &&
                    localDate.getDayOfMonth() == 31 | entry.equals(relevantPrices.lastEntry())) {
                BigDecimal percentProfitComparedToStartOfYear;
                BigDecimal depositsThisYear;
                BigDecimal startPosition;
                if(yearOverYearDataList.size() > 0) {
                    depositsThisYear = totalEurDeposited.subtract(yearOverYearDataList.getLast().getTotalAmountEurAtMomentOfDepositing());
                    YearOverYearData yearOverYearDataPreviousYear = yearOverYearDataList.getLast();

                    startPosition = yearOverYearDataPreviousYear.getTotalAmountEurValueNow();
                } else {
                    depositsThisYear = totalEurDeposited;
                    startPosition = BigDecimal.ZERO;
                }
                if (totalEurValueNow.compareTo(depositsThisYear) > 0) {
                    // profit
                    BigDecimal totalInvestment = startPosition.add(depositsThisYear);
                    BigDecimal netProfit = totalEurValueNow.subtract(totalInvestment);

                    percentProfitComparedToStartOfYear = netProfit.divide(totalInvestment, 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
                } else {
                    // loss
                    BigDecimal startPlusDeposit = startPosition.add(depositsThisYear);
                    BigDecimal netLoss = startPlusDeposit.subtract(totalEurValueNow);
                    percentProfitComparedToStartOfYear = netLoss.divide(startPlusDeposit, 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).negate();
                }

                YearOverYearData yearOverYearData = new YearOverYearData(localDate.getYear(), totalSats, totalEurValueNow, totalEurDeposited, percentProfitComparedToStartOfYear, depositsThisYear);
                yearOverYearDataList.add(yearOverYearData);
            }
        }

        return new DailyDatasAndYoYs(dailyDataList, yearOverYearDataList);
    }

}
