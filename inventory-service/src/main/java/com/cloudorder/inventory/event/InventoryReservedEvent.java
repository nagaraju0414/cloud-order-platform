package com.cloudorder.inventory.event;

import java.time.Instant;

public record InventoryReservedEvent(

        String eventId,

        String eventType,

        String orderId,

        String customerId,

        Instant occurredAt

) {
}