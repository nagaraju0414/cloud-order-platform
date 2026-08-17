package com.cloudorder.inventory.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(

        String eventId,

        String eventType,

        String orderId,

        String customerId,

        BigDecimal totalAmount,

        Instant occurredAt

) {
}