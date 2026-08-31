package com.cloudorder.inventory.dto;

import com.cloudorder.inventory.domain.InventoryItem;

import java.time.Instant;

public record InventoryResponse(

        String productId,

        String productName,

        int availableQuantity,

        int reservedQuantity,

        Instant updatedAt

) {

    public static InventoryResponse from(
            InventoryItem item) {

        return new InventoryResponse(

                item.productId(),

                item.productName(),

                item.availableQuantity(),

                item.reservedQuantity(),

                item.updatedAt()
        );
    }
}