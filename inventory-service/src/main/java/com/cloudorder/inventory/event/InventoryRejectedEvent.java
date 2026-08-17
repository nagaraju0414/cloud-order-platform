package com.cloudorder.inventory.event;

import java.time.Instant;

public record InventoryRejectedEvent(

        String eventId,

        String eventType,

        String orderId,

        String reason,

        Instant occurredAt

) {
}