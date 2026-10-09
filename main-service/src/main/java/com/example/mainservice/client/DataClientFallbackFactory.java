package com.example.mainservice.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class DataClientFallbackFactory implements FallbackFactory<DataClient> {

    @Override
    public DataClient create(Throwable cause) {
        System.err.println("Data Service fallback triggered: " + cause.getMessage());
        return new DataClient() {
            @Override
            public String getMessage() {
                return "Fallback: Data Service is currently unavailable. Reason: " + cause.getMessage();
            }
        };
    }
}
