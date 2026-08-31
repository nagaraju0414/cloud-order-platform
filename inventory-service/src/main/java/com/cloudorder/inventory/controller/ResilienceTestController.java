package com.cloudorder.inventory.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResilienceTestController {

    @GetMapping("/test/downstream")
    public String downstream(
            @RequestParam(defaultValue = "1000") long delay,
            @RequestParam(defaultValue = "false") boolean fail) {

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Request interrupted");
        }

        if (fail) {
            throw new IllegalStateException("Downstream service failed");
        }

        return "DOWNSTREAM SUCCESS";
    }
}
