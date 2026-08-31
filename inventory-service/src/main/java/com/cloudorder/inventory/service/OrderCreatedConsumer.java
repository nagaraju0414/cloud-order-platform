package com.cloudorder.inventory.service;

import com.cloudorder.inventory.event.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderCreatedConsumer {

    private static final String
            ORDER_CREATED_TOPIC =
            "order.created";

    private static final String
            INVENTORY_RESERVED_TOPIC =
            "inventory.reserved";

    private static final String
            INVENTORY_REJECTED_TOPIC =
            "inventory.rejected";

    private final ObjectMapper objectMapper;

    private final InventoryService inventoryService;

    private final KafkaTemplate<String, String>
            kafkaTemplate;

    public OrderCreatedConsumer(

            ObjectMapper objectMapper,

            InventoryService inventoryService,

            KafkaTemplate<String, String>
                    kafkaTemplate) {

        this.objectMapper =
                objectMapper;

        this.inventoryService =
                inventoryService;

        this.kafkaTemplate =
                kafkaTemplate;
    }

    @KafkaListener(
            topics = ORDER_CREATED_TOPIC,
            groupId = "inventory-service"
    )
    public void consume(String message) {

        try {

            OrderCreatedEvent event =
                    objectMapper.readValue(
                            message,
                            OrderCreatedEvent.class
                    );

            process(event);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to process order.created",
                    e
            );
        }
    }

    private void process(
            OrderCreatedEvent event) {

        /*
         * Day 6 simplification:
         *
         * Order currently contains totalAmount,
         * not productId/quantity.
         *
         * We will enhance the order model
         * with order lines in a later day.
         */

        String productId =
                "PRODUCT-001";

        int quantity = 1;

        boolean reserved =
                inventoryService.reserve(
                        productId,
                        quantity
                );

        if (reserved) {

            publishReserved(event);

        } else {

            publishRejected(
                    event,
                    "INSUFFICIENT_STOCK"
            );
        }
    }

    private void publishReserved(
            OrderCreatedEvent event) {

        InventoryReservedEvent reservedEvent =
                new InventoryReservedEvent(

                        UUID.randomUUID()
                                .toString(),

                        "INVENTORY_RESERVED",

                        event.orderId(),

                        event.customerId(),

                        Instant.now()
                );

        try {

            kafkaTemplate.send(

                    INVENTORY_RESERVED_TOPIC,

                    event.orderId(),

                    objectMapper.writeValueAsString(
                            reservedEvent
                    )
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to publish inventory.reserved",
                    e
            );
        }
    }

    private void publishRejected(

            OrderCreatedEvent event,

            String reason) {

        InventoryRejectedEvent rejectedEvent =
                new InventoryRejectedEvent(

                        UUID.randomUUID()
                                .toString(),

                        "INVENTORY_REJECTED",

                        event.orderId(),

                        reason,

                        Instant.now()
                );

        try {

            kafkaTemplate.send(

                    INVENTORY_REJECTED_TOPIC,

                    event.orderId(),

                    objectMapper.writeValueAsString(
                            rejectedEvent
                    )
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to publish inventory.rejected",
                    e
            );
        }
    }
}