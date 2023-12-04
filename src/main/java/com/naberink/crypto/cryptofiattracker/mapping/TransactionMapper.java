package com.naberink.crypto.cryptofiattracker.mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import com.naberink.crypto.cryptofiattracker.Util;
import com.naberink.crypto.cryptofiattracker.blockstream.response.Transaction;
import com.naberink.crypto.cryptofiattracker.blockstream.response.Vin;
import com.naberink.crypto.cryptofiattracker.blockstream.response.Vout;
import com.naberink.crypto.cryptofiattracker.dto.MappedTransaction;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TransactionMapper {
    public static List<MappedTransaction> mapTransactions(List<Transaction> transactions, String address,
            ConcurrentSkipListMap<Long, BigDecimal> priceMap) {
        BigDecimal latestPrice = priceMap.lastEntry().getValue();
        List<MappedTransaction> mappedTransactions = new ArrayList<>();
        long satsBalance = 0;
        BigDecimal eurValueInvested = BigDecimal.ZERO;

        // Reverse the transactions list for processing
        Collections.reverse(transactions);

        for (Transaction transaction : transactions) {
            if (!transaction.getStatus().isConfirmed()) {
                continue;
            }

            long amountSats = calculateNetSats(transaction, address);
            satsBalance += amountSats;

            long blockTimeEpochSeconds = transaction.getStatus().getBlockTime();
            BigDecimal bitcoinPriceAtBlocktime = calculatePriceForNearestPrice(priceMap, blockTimeEpochSeconds)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal fiatPriceAtBlockTime = Util.calculateFiatPrice(amountSats, bitcoinPriceAtBlocktime);
            eurValueInvested = eurValueInvested.add(fiatPriceAtBlockTime);

            Instant timestamp = Instant.ofEpochSecond(blockTimeEpochSeconds);
            BigDecimal totalAmountEurValueNow = Util.calculateFiatPrice(satsBalance, latestPrice);
            BigDecimal txAmountEurNow = Util.calculateFiatPrice(amountSats, latestPrice);

            mappedTransactions.add(new MappedTransaction(transaction.getTxid(), amountSats, fiatPriceAtBlockTime,
                    eurValueInvested, timestamp, satsBalance, totalAmountEurValueNow, txAmountEurNow));
        }

        return mappedTransactions;
    }

    private static long calculateNetSats(Transaction transaction, String address) {
        long amountSats = 0;
        for (Vout vout : transaction.getVout()) {
            if (address.equals(vout.getScriptpubkeyAddress())) {
                amountSats += vout.getValue();
            }
        }
        for (Vin vin : transaction.getVin()) {
            Vout prevout = vin.getPrevout();
            if (prevout != null && address.equals(prevout.getScriptpubkeyAddress())) {
                amountSats -= prevout.getValue();
            }
        }
        return amountSats;
    }

    /**
     * Calculate the amounts multiplied by the nearest price.
     *
     * @return A List of calculated values.
     */
    public static BigDecimal calculatePriceForNearestPrice(ConcurrentSkipListMap<Long, BigDecimal> pricesMap,
            long epochSeconds) {
        long epochMillis = epochSeconds * 1000;

        // Find the closest epoch in the prices map
        Long floorKey = pricesMap.floorKey(epochMillis);
        Long ceilingKey = pricesMap.ceilingKey(epochMillis);

        // Determine the nearest epoch
        Long nearestEpoch = Util.getNearestEpoch(epochMillis, floorKey, ceilingKey);

        // Get the price for the nearest epoch
        return pricesMap.get(nearestEpoch);
    }

}
