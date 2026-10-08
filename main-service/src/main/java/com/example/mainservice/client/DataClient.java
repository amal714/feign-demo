package com.example.mainservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "data-service", url = "http://localhost:8101")
public interface DataClient {

    @GetMapping("/message")
    String getMessage();
}
