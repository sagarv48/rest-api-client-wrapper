package com.sagarv.webclient.common;

import com.sagarv.webclient.RequestBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public abstract class AbstractRequestExecutor {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractRequestExecutor.class);

    protected final WebClient webClient;
    protected final CircuitBreaker circuitBreaker;

    protected AbstractRequestExecutor(WebClient webClient, CircuitBreaker circuitBreaker) {
        this.webClient = webClient;
        this.circuitBreaker = circuitBreaker;
    }

    protected <R> Mono<R> executeMono(HttpMethod method, String url, Object body, Map<String, String> headers, Class<R> responseType) {
        return new RequestBuilder(webClient, method, url)
                .headers(headers)
                .body(body)
                .executeReactive(responseType)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .doOnSubscribe(sub -> logger.info("Executing {} request to {}", method, url))
                .doOnSuccess(resp -> logger.info("Request to {} was successful", url))
                .doOnError(error -> logger.error("Request to {} failed", url, error));
    }

    protected <R> Mono<List<R>> executeMonoList(HttpMethod method, String url, Object body, Map<String, String> headers, ParameterizedTypeReference<List<R>> responseType) {
        return new RequestBuilder(webClient, method, url)
                .headers(headers)
                .body(body)
                .executeReactive(responseType)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .doOnSubscribe(sub -> logger.info("Executing {} request to {}", method, url))
                .doOnSuccess(resp -> logger.info("Request to {} was successful", url))
                .doOnError(error -> logger.error("Request to {} failed", url, error));
    }

    protected <R> Flux<R> executeFlux(HttpMethod method, String url, Object body, Map<String, String> headers, Class<R> responseType) {
        return new RequestBuilder(webClient, method, url)
                .headers(headers)
                .body(body)
                .executeReactiveAsFlux(responseType)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .doOnSubscribe(sub -> logger.info("Executing {} request to {}", method, url))
                .doOnError(error -> logger.error("Request to {} failed", url, error));
    }
}