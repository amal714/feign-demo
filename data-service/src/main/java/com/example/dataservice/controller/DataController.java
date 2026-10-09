package com.example.dataservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    @GetMapping("/message")
    public String message() {
        System.out.println("reached inside message() method inside data-service controller class");
        return "Hello from Data Service!";
    }
}
