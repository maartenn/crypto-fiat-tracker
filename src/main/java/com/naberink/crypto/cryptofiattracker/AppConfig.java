package com.naberink.crypto.cryptofiattracker;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableScheduling
public class AppConfig {
    @Bean
    public ConcurrentSkipListMap<Long, BigDecimal> priceMap() {
        return new ConcurrentSkipListMap<>();
    }

    @Bean
    @Primary
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    @Bean(name = "transactionMapper")
    public ObjectMapper transactionMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public HttpClient getHttpClient() {
        return HttpClient.newHttpClient();
    }
}
