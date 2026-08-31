package com.cloudorder.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {

    private String eventId;

    private String orderId;

    private String paymentId;

    private String reason;

    private String timestamp;
}

