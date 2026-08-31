package com.cloudorder.apigateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/order")
    public org.springframework.http.ResponseEntity<Map<String, Object>>
    orderFallback(ServerWebExchange exchange) {

        return org.springframework.http.ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Order service unavailable",
                        "message", "Please try again later"
                ));
    }
}