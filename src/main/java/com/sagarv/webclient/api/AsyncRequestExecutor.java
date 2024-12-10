package com.sagarv.webclient.api;

import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface AsyncRequestExecutor {
    <R> Mono<R> execute(HttpMethod method, String url, Map<String, String> headers, Class<R> responseType);
    <B, R> Mono<R> execute(HttpMethod method, String url, B body, Map<String, String> headers, Class<R> responseType);
}
