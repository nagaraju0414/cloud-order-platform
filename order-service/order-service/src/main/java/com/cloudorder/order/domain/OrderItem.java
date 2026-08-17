package com.cloudorder.order.domain;

import java.math.BigDecimal;

public record OrderItem(
        String productId,
        int quantity,
        BigDecimal unitPrice
) {
}