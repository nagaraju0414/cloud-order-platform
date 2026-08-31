package com.cloudorder.order.client;

import com.cloudorder.order.exception.InventoryServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(RestClient.Builder builder, @Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.restClient = builder
                .baseUrl(inventoryServiceUrl)
                .build();
    }
    @CircuitBreaker(name = "inventoryCircuitBreaker",fallbackMethod = "inventoryFallback"  )
    @Retry(name = "inventoryRetry")
    @TimeLimiter(name = "inventoryTimeout")
    public String checkInventory(String productId) {
        log.info("==========Invoking Checking inventory for product: " + productId);
        return restClient
                .get()
                .uri("/inventory/{productId}", productId)
                .retrieve()
                .body(String.class);
    }

    public CompletableFuture<String> inventoryFallback(
            String productId,
            Throwable throwable) {

        log.info(
                "Inventory fallback triggered for: " + productId
        );

        log.info(
                "Reason: " + throwable.getClass().getSimpleName()
        );

        return CompletableFuture.failedFuture(
                new InventoryServiceException(
                        "Inventory service is temporarily unavailable",
                        throwable
                ));
    }
}