package com.cloudorder.order.controller;

import com.cloudorder.order.dto.CreateOrderRequest;
import com.cloudorder.order.dto.OrderResponse;
import com.cloudorder.order.service.OrderApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(
            OrderApplicationService orderApplicationService) {

        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return orderApplicationService.createOrder(request);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @PathVariable String orderId) {

        return orderApplicationService.getOrder(orderId);
    }

    @GetMapping
    public List<OrderResponse> getOrders(
            @RequestParam String customerId) {

        return orderApplicationService
                .getOrdersByCustomer(customerId);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable String orderId) {

        return orderApplicationService.cancelOrder(orderId);
    }
}