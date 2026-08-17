package com.cloudorder.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(

        @NotBlank
        String productId,

        @Min(1)
        int quantity,

        @NotNull
        BigDecimal unitPrice
) {
}