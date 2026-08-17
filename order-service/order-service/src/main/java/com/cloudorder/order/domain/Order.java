package com.cloudorder.order.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Order(
        String orderId,

        String customerId,

        BigDecimal totalAmount,

        OrderStatus status,

        Instant createdAt,

        Instant updatedAt
) {
}