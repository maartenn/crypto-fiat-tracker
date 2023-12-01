package com.naberink.crypto.cryptofiattracker.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

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

    @GetMapping("/transactions/{address}")
    public Response transactions(@PathVariable("address") String address) {
        log.info("Fetching data for address: {}", address);
        // Removed explicit System.out.println statements, consider using a logger
        // instead

        var priceMap = pricesService.getPriceMap();
        // Logging size of pricemap, consider using logger.debug if necessary

        var transactions = getTransactions(address);

        if (transactions.isEmpty()) {
            return null; // Consider returning a response indicating no transactions
        }

        var mappedTransactions = TransactionMapper.mapTransactions(transactions, address, priceMap);
        var dailyDataList = calculateDailyDataList(mappedTransactions, priceMap);

        MappedTransaction last = mappedTransactions.getLast();
        BigDecimal profit = last.getTotalAmountEurValueNow().subtract(last.getTotalAmountEurAtMomentOfDepositing());
        BigDecimal percentProfit = profit.divide(last.getTotalAmountEurAtMomentOfDepositing(), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);

        return new Response(dailyDataList, mappedTransactions, last.getTotalAmountEurAtMomentOfDepositing(),
                last.getTotalAmountEurValueNow(), last.getTotalSatsBalance(), percentProfit);
    }

    private List<Transaction> getTransactions(String address) {
        try {
            return blockStreamApi.getTransactionsForAddress(address);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt(); // Proper handling of InterruptedException
            throw new RuntimeException("Error fetching transactions for address: " + address, e);
        }
    }

    private List<DailyData> calculateDailyDataList(List<MappedTransaction> mappedTransactions,
            NavigableMap<Long, BigDecimal> priceMap) {
        List<DailyData> dailyDataList = new ArrayList<>();
        // TODO use yoy
        //List<YearOverYearData> yearOverYearData = new ArrayList<>();
        if (mappedTransactions.isEmpty()) {
            return dailyDataList; // Return an empty list if there are no transactions
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
            // if day is jan 1 then create YearOverYearData filling all fields in
            // constructor and add to the list

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
        }

        return dailyDataList;
    }

}
