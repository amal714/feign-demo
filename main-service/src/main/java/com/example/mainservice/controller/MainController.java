package com.example.mainservice.controller;

import com.example.mainservice.client.DataClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    private final DataClient dataClient;
    private final RestTemplate restTemplate;
    private final RestClient restClient;

    public MainController(DataClient dataClient, RestTemplate restTemplate, RestClient restClient) {
        this.dataClient = dataClient;
        this.restTemplate = restTemplate;
        this.restClient = restClient;
    }

    @GetMapping("/greetUsingFeignClient")
    public String greet() {
        return dataClient.getMessage();
    }

    @GetMapping("/greetUsingRestTemplate")
    public String greetUsingRestTemplate() {
        return restTemplate.getForObject("http://localhost:8101/message", String.class);
    }

    @GetMapping("/greetUsingRestClient")
    public String greetUsingRestClient() {
        return restClient.get()
                .uri("http://localhost:8101/message")
                .retrieve()
                .body(String.class);
    }
}
