package com.naberink.crypto.cryptofiattracker.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import com.litesoftwares.coingecko.domain.Coins.MarketChart;
import com.litesoftwares.coingecko.impl.CoinGeckoApiClientImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricesService {
    private final ConcurrentSkipListMap<Long, BigDecimal> priceMap;

    public ConcurrentSkipListMap<Long, BigDecimal> getPriceMap() {
        if (priceMap == null || priceMap.size() == 0) {
            fillPriceMap();
        }
        return priceMap;
    }

    /**
     * Because the api-client wants an int as param (instead of 'max' as is on api)
     * we need to calc the days between now and the first price data.
     *
     * @return number of days between now and day of first price data.
     */
    private int getDaysFromStart() {
        long epochMillis = 1367107200000L;

        // Converteer epoch naar Instant
        Instant epochInstant = Instant.ofEpochMilli(epochMillis);

        // Huidige tijd als Instant
        Instant nowInstant = Instant.now();

        // Bereken het verschil in dagen
        return (int) ChronoUnit.DAYS.between(epochInstant, nowInstant); // casting to int shouldn't be a problem till
                                                                        // very long in the future...
    }

    // @Retry(name = "fill price map from coingecko data")
    private void fillPriceMap() {
        log.info("start refilling pricemap: " + Instant.now());

        CoinGeckoApiClientImpl coinGeckoApiClient = new CoinGeckoApiClientImpl();

        MarketChart btcChart = coinGeckoApiClient.getCoinMarketChartById("bitcoin", "eur", getDaysFromStart());
        List<List<String>> prices = btcChart.getPrices();
        // start with newest and add until we see that element has already been added
        // (ergo list complete up to date).
        for (List<String> price : prices.reversed()) {
            if (priceMap.putIfAbsent(Long.parseLong(price.get(0)), new BigDecimal(price.get(1))) != null) {
                // the price is already
                break;
            }
        }
        ;
        log.info("finished refilling pricemap: " + Instant.now());

    }

    //
    //
    @PostConstruct
    public void initPriceMap() {
        // initial filling
        getPriceMap();
    }

    @Scheduled(cron = "0 40 0 * * *") // At 00:40 UTC every day
    public void refill() {
        // initial filling
        fillPriceMap();
    }
}
