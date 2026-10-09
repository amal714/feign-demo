package com.example.dataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DataApplication {

    public static void main(String[] args) {
        System.out.println("reached inside main method of data-service. about to start the spring application context");
        SpringApplication.run(DataApplication.class, args);
    }
}
