package com.example.mainservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "DATA-SERVICE")
public interface DataClient {

    @GetMapping("/message")
    String getMessage();
}