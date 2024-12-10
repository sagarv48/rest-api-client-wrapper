package com.sagarv.webclient.api;

import org.springframework.http.HttpMethod;

import java.util.Map;

public interface SyncRequestExecutor {
    <R> R execute(HttpMethod method, String url, Map<String, String> headers, Class<R> responseType);
    <B, R> R execute(HttpMethod method, String url, B body, Map<String, String> headers, Class<R> responseType);
}