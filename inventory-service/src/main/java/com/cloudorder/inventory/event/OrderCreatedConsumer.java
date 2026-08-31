/*package com.cloudorder.inventory.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
public class OrderCreatedConsumer {

    @KafkaListener(
            topics = "order.created",
            groupId = "inventory-service"
    )
    public void consume(OrderCreatedEvent event) {

        log.info(
                "Processing OrderCreated eventId={} orderId={} customerId={}",
                event.eventId(),
                event.orderId(),
                event.customerId()
        );
    }
}*/