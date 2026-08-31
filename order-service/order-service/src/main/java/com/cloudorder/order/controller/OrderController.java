package com.cloudorder.order.controller;

import com.cloudorder.order.client.InventoryClient;
import com.cloudorder.order.domain.Order;

import com.cloudorder.order.dto.CreateOrderRequest;
import com.cloudorder.order.dto.OrderResponse;

import com.cloudorder.order.service.OrderApplicationService;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {

    private final OrderApplicationService service;

    private final InventoryClient inventoryClient;


    public OrderController(
            OrderApplicationService service,InventoryClient inventoryClient) {

        this.service = service;
        this.inventoryClient = inventoryClient;
    }

    @PostMapping
    public ResponseEntity<OrderResponse>
    createOrder(

            @Valid
            @RequestBody
            CreateOrderRequest request) {

        Order order =
                service.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        OrderResponse.from(order)
                );
    }

    @GetMapping("/inventory/{productId}")
    public String checkInventory(@PathVariable  String productId) {
        log.info("==========Checking inventory for product: " + productId);
        return inventoryClient.checkInventory(productId);
    }

    @GetMapping("/test/downstream")
    public String downstream() {
        return "DOWNSTREAM SUCCESS";
    }

}