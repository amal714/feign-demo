package com.example.mainservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "DATA-SERVICE", fallbackFactory = DataClientFallbackFactory.class)
public interface DataClient {

    @GetMapping("/message")
    String getMessage();
}