package com.cloudorder.inventory.domain;

import java.time.Instant;

public record InventoryItem(

        String productId,

        String productName,

        int availableQuantity,

        int reservedQuantity,

        Instant updatedAt

) {
}