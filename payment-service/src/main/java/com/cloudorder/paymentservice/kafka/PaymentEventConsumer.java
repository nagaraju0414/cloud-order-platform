package com.cloudorder.paymentservice.kafka;
import com.cloudorder.paymentservice.event.InventoryReservedEvent;
import com.cloudorder.paymentservice.event.PaymentCompletedEvent;
import com.cloudorder.paymentservice.model.Payment;
import com.cloudorder.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(
            topics = "inventory-reserved",
            groupId = "payment-service-group"
    )
    public void consume(
            InventoryReservedEvent event,
            Acknowledgment acknowledgment) {

        try {

            Payment payment =
                    paymentService.processPayment(event);

            PaymentCompletedEvent completedEvent =
                    PaymentCompletedEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .orderId(payment.getOrderId())
                            .paymentId(payment.getPaymentId())
                            .status(payment.getStatus().name())
                            .timestamp(Instant.now().toString())
                            .build();

            paymentEventProducer
                    .publishPaymentCompleted(completedEvent);

            acknowledgment.acknowledge();

        } catch (Exception ex) {

            // Don't acknowledge.
            // Kafka can redeliver the message.

            throw ex;
        }
    }
}