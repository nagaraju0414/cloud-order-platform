package com.cloudorder.order.service;

import com.cloudorder.order.event.OrderCreatedEvent;
import com.cloudorder.order.repository.OutboxItem;
import com.cloudorder.order.repository.OutboxStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

@Component
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public OutboxEventFactory(
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    public OutboxItem create(
            OrderCreatedEvent event) {

        try {

            String now =
                    event.occurredAt().toString();

            OutboxItem item =
                    new OutboxItem();

            item.setPk(
                    "OUTBOX#" +
                            event.eventId()
            );

            item.setSk(
                    "OUTBOX#" +
                            event.eventId()
            );

            item.setGsi1pk(
                    "OUTBOX#PENDING"
            );

            item.setGsi1sk(
                    now +
                            "#" +
                            event.eventId()
            );

            item.setEventId(
                    event.eventId()
            );

            item.setEventType(
                    event.eventType()
            );

            item.setAggregateType(
                    "ORDER"
            );

            item.setAggregateId(
                    event.orderId()
            );

            item.setPayload(
                    objectMapper.writeValueAsString(
                            event
                    )
            );

            item.setStatus(
                    OutboxStatus.PENDING.name()
            );

            item.setRetryCount(0);

            item.setCreatedAt(now);

            item.setNextAttemptAt(now);

            return item;

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Unable to serialize outbox event",
                    e
            );
        }
    }
}