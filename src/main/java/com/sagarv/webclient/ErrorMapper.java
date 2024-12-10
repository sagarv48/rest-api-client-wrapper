package com.sagarv.webclient;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ErrorMapper {
    private final Map<Integer, Function<WebClientResponseException, RuntimeException>> exceptionMappings = new HashMap<>();

    public ErrorMapper map(int statusCode, Function<WebClientResponseException, RuntimeException> exceptionSupplier) {
        exceptionMappings.put(statusCode, exceptionSupplier);
        return this;
    }

    public RuntimeException mapException(WebClientResponseException e) {
        return exceptionMappings.getOrDefault(e.getRawStatusCode(), ex -> new RestClientException("Unexpected error: " + e.getResponseBodyAsString(), e)).apply(e);
    }
}