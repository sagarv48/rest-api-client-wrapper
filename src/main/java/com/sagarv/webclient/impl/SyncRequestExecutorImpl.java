package com.sagarv.webclient.impl;

import com.sagarv.webclient.api.ListRequestExecutor;
import com.sagarv.webclient.api.SyncRequestExecutor;
import com.sagarv.webclient.common.AbstractRequestExecutor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

public class SyncRequestExecutorImpl extends AbstractRequestExecutor implements SyncRequestExecutor, ListRequestExecutor {

    public SyncRequestExecutorImpl(WebClient webClient, CircuitBreaker circuitBreaker) {
        super(webClient, circuitBreaker);
    }

    @Override
    public <R> R execute(HttpMethod method, String url, Map<String, String> headers, Class<R> responseType) {
        return executeMono(method, url, null, headers, responseType).block();
    }

    @Override
    public <B, R> R execute(HttpMethod method, String url, B body, Map<String, String> headers, Class<R> responseType) {
        return executeMono(method, url, body, headers, responseType).block();
    }

    @Override
    public <R> List<R> execute(HttpMethod method, String url, Map<String, String> headers, ParameterizedTypeReference<List<R>> responseType) {
        return executeMonoList(method, url, null, headers, responseType).block();
    }

    @Override
    public <B, R> List<R> execute(HttpMethod method, String url, B body, Map<String, String> headers, ParameterizedTypeReference<List<R>> responseType) {
        return executeMonoList(method, url, body, headers, responseType).block();
    }
}
