package com.sagarv.webclient.api;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

public interface ListRequestExecutor {
    <R> List<R> execute(HttpMethod method, String url, Map<String, String> headers, ParameterizedTypeReference<List<R>> responseType);
    <B, R> List<R> execute(HttpMethod method, String url, B body, Map<String, String> headers, ParameterizedTypeReference<List<R>> responseType);
}
