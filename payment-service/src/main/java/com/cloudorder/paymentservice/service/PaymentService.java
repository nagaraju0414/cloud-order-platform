package com.cloudorder.paymentservice.service;


import com.cloudorder.paymentservice.event.InventoryReservedEvent;
import com.cloudorder.paymentservice.model.Payment;
import com.cloudorder.paymentservice.model.PaymentStatus;
import com.cloudorder.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment processPayment(
            InventoryReservedEvent event) {

        String paymentId = "PAYMENT-" + UUID.randomUUID();

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(event.getAmount())
                .status(PaymentStatus.PROCESSING)
                .idempotencyKey(event.getEventId())
                .createdAt(Instant.now().toString())
                .updatedAt(Instant.now().toString())
                .build();

        paymentRepository.save(payment);

        // Temporary payment simulation.
        // External payment gateway will be added later.

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setUpdatedAt(Instant.now().toString());

        paymentRepository.save(payment);

        return payment;
    }
}
