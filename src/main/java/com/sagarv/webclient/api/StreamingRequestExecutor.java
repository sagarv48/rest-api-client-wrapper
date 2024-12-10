package com.sagarv.webclient.api;

import org.springframework.http.HttpMethod;
import reactor.core.publisher.Flux;

import java.util.Map;

public interface StreamingRequestExecutor {
    <R> Flux<R> execute(HttpMethod method, String url, Map<String, String> headers, Class<R> responseType);
    <B, R> Flux<R> execute(HttpMethod method, String url, B body, Map<String, String> headers, Class<R> responseType);
}
