package com.naberink.crypto.cryptofiattracker.blockstream;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naberink.crypto.cryptofiattracker.blockstream.response.Transaction;

@Service
@DependsOn("pricesService")
public class BlockStreamApi {
    public static final String BASE_URL_TX_FROM_ADDRESS = "https://blockstream.info/api/address/%s/txs/chain/%s";

    private final ObjectMapper transactionMapper;

    private final HttpClient httpClient;

    public BlockStreamApi(@Qualifier("transactionMapper") ObjectMapper transactionMapper, HttpClient httpClient) {
        this.transactionMapper = transactionMapper;
        this.httpClient = httpClient;
    }

    // public List<Transaction> getTransactionsForAddress(String address) throws
    // IOException, InterruptedException {
    ////
    //// HttpClient client = HttpClient.newHttpClient();
    //// HttpRequest request = HttpRequest.newBuilder()
    //// .uri(URI.create(BASE_URL_TX_FROM_ADDRESS.formatted(address)))
    //// .build();
    ////
    //// return client.sendAsync(request, new JsonBodyHandler<>(new
    // TypeReference<List<Transaction>>() {
    //// }))
    //// .thenApply(HttpResponse::body)
    //// .join();
    // // use params to build the uri instead of string format
    // HttpClient client = HttpClient.newHttpClient();
    // HttpRequest request = HttpRequest.newBuilder()
    // .uri(URI.create(BASE_URL_TX_FROM_ADDRESS.formatted(address, "")))
    // .build();
    //
    // CompletableFuture<String> responseFuture = client.sendAsync(request,
    // HttpResponse.BodyHandlers.ofString())
    // .thenApply(HttpResponse::body);
    //
    //// String jsonResponse = responseFuture.join();
    // try {
    // String jsonResponse = responseFuture.join();
    // ObjectMapper objectMapper = new ObjectMapper();
    // objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
    // false); // we don't need a lot of properties
    //
    // List<Transaction> transactions = objectMapper.readValue(jsonResponse, new
    // TypeReference<List<Transaction>>() {
    // });
    //
    // return transactions;
    // } catch (Exception e) {
    // // Log the exception and the jsonResponse (if available) for debugging
    // e.printStackTrace();
    // return Collections.emptyList();
    // }
    // }
    public List<Transaction> getTransactionsForAddress(String address) throws IOException, InterruptedException {
        List<Transaction> allTransactions = new ArrayList<>();
        String lastSeenTxid = "";
        do {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL_TX_FROM_ADDRESS.formatted(address, lastSeenTxid)))
                    .build();

            String jsonResponse = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body).join();
try {
    List<Transaction> transactions = transactionMapper.readValue(jsonResponse,
        new TypeReference<List<Transaction>>() {
        });
    if (!transactions.isEmpty()) {
        allTransactions.addAll(transactions);
        lastSeenTxid = transactions.get(transactions.size() - 1).getTxid();
    } else {
        break;
    }
} catch (Exception e) {
    // Log the exception and the jsonResponse (if available) for debugging
    e.printStackTrace();
    return Collections.emptyList();
}


        } while (true);

        return allTransactions;
    }

}
