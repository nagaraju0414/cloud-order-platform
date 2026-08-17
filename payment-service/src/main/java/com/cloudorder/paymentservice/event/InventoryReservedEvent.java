package com.cloudorder.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent {

    private String eventId;

    private String orderId;

    private String customerId;

    private BigDecimal amount;

    private String reservationId;

    private String timestamp;
}