package com.sagarv.webclient.impl;

import com.sagarv.webclient.api.AsyncRequestExecutor;
import com.sagarv.webclient.common.AbstractRequestExecutor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

public class AsyncRequestExecutorImpl extends AbstractRequestExecutor implements AsyncRequestExecutor {

    public AsyncRequestExecutorImpl(WebClient webClient, CircuitBreaker circuitBreaker) {
        super(webClient, circuitBreaker);
    }

    @Override
    public <R> Mono<R> execute(HttpMethod method, String url, Map<String, String> headers, Class<R> responseType) {
        return executeMono(method, url, null, headers, responseType);
    }

    @Override
    public <B, R> Mono<R> execute(HttpMethod method, String url, B body, Map<String, String> headers, Class<R> responseType) {
        return executeMono(method, url, body, headers, responseType);
    }
}
