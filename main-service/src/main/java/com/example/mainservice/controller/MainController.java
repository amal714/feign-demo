package com.example.mainservice.controller;

import com.example.mainservice.client.DataClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger logger1 = LoggerFactory.getLogger(MainController.class);

    @GetMapping("/greetUsingFeignClient")
    public String greet() {
//        System.out.println("reached inside greet() method inside main-service controller class, going to call dataClient.getMessage() method");
        logger1.info("reached inside greet() method inside main-service controller class, going to call dataClient.getMessage() method");
        return dataClient.getMessage();
    }
    @GetMapping("/greetUsingRestTemplate")
    public String greetUsingRestTemplate() {
        return restTemplate.getForObject("http://DATA-SERVICE/message", String.class);
    }

    @GetMapping("/greetUsingRestClient")
    public String greetUsingRestClient() {
        return restClient.get()
                .uri("http://DATA-SERVICE/message")
                .retrieve()
                .body(String.class);
    }
}
