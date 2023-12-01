package com.naberink.crypto.cryptofiattracker.blockstream;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class JsonBodyHandler<W> implements BodyHandler<W> {
    private final TypeReference<W> typeRef;

    public JsonBodyHandler(TypeReference<W> typeRef) {
        this.typeRef = typeRef;
    }

    @Override
    public BodySubscriber<W> apply(ResponseInfo responseInfo) {
        ObjectMapper objectMapper = new ObjectMapper(); // TODO make this a bean
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();

        return HttpResponse.BodySubscribers.mapping(
                upstream,
                (InputStream inputStream) -> {
                    try {
                        return objectMapper.readValue(inputStream, typeRef);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
