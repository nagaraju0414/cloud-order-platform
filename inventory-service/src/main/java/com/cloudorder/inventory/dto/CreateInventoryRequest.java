package com.cloudorder.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateInventoryRequest(

        @NotBlank
        String productId,

        @NotBlank
        String productName,

        @Min(0)
        int quantity

) {
}