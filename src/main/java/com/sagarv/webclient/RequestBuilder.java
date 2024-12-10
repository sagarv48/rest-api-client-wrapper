package com.sagarv.webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RequestBuilder.class);

    private final WebClient webClient;
    private final HttpMethod method;
    private final String url;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<Integer, Function<WebClientResponseException, ? extends RuntimeException>> exceptionMappings = new HashMap<>();
    private Object body;
    private Duration timeout = Duration.ofSeconds(10); // Default timeout
    private int retryCount = 0;

    public RequestBuilder(WebClient webClient, HttpMethod method, String url) {
        this.webClient = webClient;
        this.method = method;
        this.url = url;
    }

    public RequestBuilder header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public RequestBuilder headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public RequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    public RequestBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public RequestBuilder retry(int count) {
        this.retryCount = count;
        return this;
    }

    public RequestBuilder authenticate(String token) {
        this.headers.put("Authorization", "Bearer " + token);
        return this;
    }

    public RequestBuilder exceptionMapping(int statusCode, Function<WebClientResponseException, ? extends RuntimeException> exceptionSupplier) {
        this.exceptionMappings.put(statusCode, exceptionSupplier);
        return this;
    }

    private WebClient.RequestHeadersSpec<?> prepareRequest() {
        WebClient.RequestHeadersSpec<?> requestSpec = webClient.method(method).uri(url);
        headers.forEach(requestSpec::header);
        if (body != null) {
            requestSpec = ((WebClient.RequestBodySpec) requestSpec).bodyValue(body);
        }
        return requestSpec;
    }

    public <R> Mono<R> executeReactive(Class<R> responseType) {
        return executeRequest(prepareRequest().retrieve().bodyToMono(responseType));
    }

    public <R> Mono<R> executeReactive(ParameterizedTypeReference<R> responseType) {
        return executeRequest(prepareRequest().retrieve().bodyToMono(responseType));
    }

    public <R> Flux<R> executeReactiveAsFlux(Class<R> responseType) {
        return executeRequest(prepareRequest().retrieve().bodyToFlux(responseType));
    }

    private <R> Mono<R> executeRequest(Mono<R> responseMono) {
        return responseMono
                .timeout(timeout)
                .retry(retryCount)
                .doOnSubscribe(sub -> logger.info("Executing {} request to {}", method, url))
                .doOnSuccess(resp -> logger.info("Request to {} was successful", url))
                .doOnError(error -> logger.error("Request to {} failed", url, error))
                .onErrorResume(WebClientResponseException.class, this::mapException);
    }

    private <R> Flux<R> executeRequest(Flux<R> responseFlux) {
        return responseFlux
                .timeout(timeout)
                .retry(retryCount)
                .doOnSubscribe(sub -> logger.info("Executing {} request to {}", method, url))
                .doOnError(error -> logger.error("Request to {} failed", url, error))
                .onErrorResume(WebClientResponseException.class, this::mapException);
    }

    private <T> Mono<T> mapException(WebClientResponseException e) {
        Function<WebClientResponseException, ? extends RuntimeException> exceptionSupplier = exceptionMappings.get(e.getRawStatusCode());
        if (exceptionSupplier != null) {
            return Mono.error(exceptionSupplier.apply(e));
        } else {
            return Mono.error(new RestClientException("Unexpected error: " + e.getResponseBodyAsString(), e));
        }
    }

    private <T> Flux<T> mapExceptionFlux(WebClientResponseException e) {
        Function<WebClientResponseException, ? extends RuntimeException> exceptionSupplier = exceptionMappings.get(e.getRawStatusCode());
        if (exceptionSupplier != null) {
            return Flux.error(exceptionSupplier.apply(e));
        } else {
            return Flux.error(new RestClientException("Unexpected error: " + e.getResponseBodyAsString(), e));
        }
    }
}