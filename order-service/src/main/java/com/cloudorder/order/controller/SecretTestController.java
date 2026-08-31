package com.cloudorder.order.controller;

import com.cloudorder.order.service.SecretService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class SecretTestController {

    private final SecretService secretService;

    public SecretTestController(SecretService secretService) {
        this.secretService = secretService;
    }

    @GetMapping("/internal/config/secret-status")
    public Map<String, Object> secretStatus() {

        try {
            return Map.of(
                    "secretLoaded",
                    secretService.secretExists()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}