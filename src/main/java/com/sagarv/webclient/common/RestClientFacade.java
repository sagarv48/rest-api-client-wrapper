package com.sagarv.webclient.common;

import com.sagarv.webclient.api.AsyncRequestExecutor;
import com.sagarv.webclient.api.StreamingRequestExecutor;
import com.sagarv.webclient.api.SyncRequestExecutor;
import com.sagarv.webclient.impl.AsyncRequestExecutorImpl;
import com.sagarv.webclient.impl.StreamingRequestExecutorImpl;
import com.sagarv.webclient.impl.SyncRequestExecutorImpl;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class RestClientFacade {

    private final SyncRequestExecutor syncExecutor;
    private final AsyncRequestExecutor asyncExecutor;
    private final StreamingRequestExecutor streamingExecutor;

    @Autowired
    public RestClientFacade(WebClient webClient, CircuitBreaker circuitBreaker) {
        this.syncExecutor = new SyncRequestExecutorImpl(webClient, circuitBreaker);
        this.asyncExecutor = new AsyncRequestExecutorImpl(webClient, circuitBreaker);
        this.streamingExecutor = new StreamingRequestExecutorImpl(webClient, circuitBreaker);
    }

    public SyncRequestExecutor getSyncExecutor() {
        return syncExecutor;
    }

    public AsyncRequestExecutor getAsyncExecutor() {
        return asyncExecutor;
    }

    public StreamingRequestExecutor getStreamingExecutor() {
        return streamingExecutor;
    }
}