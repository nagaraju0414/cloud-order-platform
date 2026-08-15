package com.cloudorder.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    @GetMapping("/api/v1/orders/test")
    public String test(){
        return "Order Service is running";
    }
}
