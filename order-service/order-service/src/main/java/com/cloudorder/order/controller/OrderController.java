package com.cloudorder.order.controller;

import com.cloudorder.order.domain.Order;

import com.cloudorder.order.dto.CreateOrderRequest;
import com.cloudorder.order.dto.OrderResponse;

import com.cloudorder.order.service.OrderApplicationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderApplicationService service;

    public OrderController(
            OrderApplicationService service) {

        this.service = service;
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


}