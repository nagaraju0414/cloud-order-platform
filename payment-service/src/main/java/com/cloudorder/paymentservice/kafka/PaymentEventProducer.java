package com.cloudorder.paymentservice.kafka;


import com.cloudorder.paymentservice.event.PaymentCompletedEvent;
import com.cloudorder.paymentservice.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(
            PaymentCompletedEvent event) {

        kafkaTemplate.send(
                "payment-completed",
                event.getOrderId(),
                event
        );
    }

    public void publishPaymentFailed(
            PaymentFailedEvent event) {

        kafkaTemplate.send(
                "payment-failed",
                event.getOrderId(),
                event
        );
    }
}
