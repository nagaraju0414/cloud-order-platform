package com.cloudorder.order.dto;

import com.cloudorder.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        String orderId,
        String customerId,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}