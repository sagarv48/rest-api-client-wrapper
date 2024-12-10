package com.sagarv.webclient.impl;

import com.sagarv.webclient.api.StreamingRequestExecutor;
import com.sagarv.webclient.common.AbstractRequestExecutor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

public class StreamingRequestExecutorImpl extends AbstractRequestExecutor implements StreamingRequestExecutor {

    public StreamingRequestExecutorImpl(WebClient webClient, CircuitBreaker circuitBreaker) {
        super(webClient, circuitBreaker);
    }

    @Override
    public <R> Flux<R> execute(HttpMethod method, String url, Map<String, String> headers, Class<R> responseType) {
        return executeFlux(method, url, null, headers, responseType);
    }

    @Override
    public <B, R> Flux<R> execute(HttpMethod method, String url, B body, Map<String, String> headers, Class<R> responseType) {
        return executeFlux(method, url, body, headers, responseType);
    }
}
