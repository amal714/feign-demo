package com.example.dataservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    private static Logger logger1 = LoggerFactory.getLogger(DataController.class);


    @GetMapping("/message")
    public String message() {
//        System.out.println("reached inside message() method inside data-service controller class");
        logger1.info("reached inside message() method inside data-service controller class");
        return "Hello from Data Service!";
    }
}
