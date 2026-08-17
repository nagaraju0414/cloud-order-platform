package com.cloudorder.order.service;

import com.cloudorder.order.domain.Order;
import com.cloudorder.order.domain.OrderStatus;

import com.cloudorder.order.dto.CreateOrderRequest;

import com.cloudorder.order.event.OrderCreatedEvent;

import com.cloudorder.order.repository.OutboxItem;
import com.cloudorder.order.repository.TransactionalOrderRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderApplicationService {

    private final TransactionalOrderRepository
            repository;

    private final OutboxEventFactory
            outboxEventFactory;

    public OrderApplicationService(

            TransactionalOrderRepository repository,

            OutboxEventFactory outboxEventFactory) {

        this.repository =
                repository;

        this.outboxEventFactory =
                outboxEventFactory;
    }

    public Order createOrder(
            CreateOrderRequest request) {

        Instant now =
                Instant.now();

        Order order =
                new Order(

                        UUID.randomUUID()
                                .toString(),

                        request.customerId(),

                        request.totalAmount(),

                        OrderStatus.CREATED,

                        now,

                        now
                );

        OrderCreatedEvent event =
                new OrderCreatedEvent(

                        UUID.randomUUID()
                                .toString(),

                        "ORDER_CREATED",

                        order.orderId(),

                        order.customerId(),

                        order.totalAmount(),

                        now
                );

        OutboxItem outbox =
                outboxEventFactory
                        .create(event);

        repository.save(
                order,
                outbox
        );

        return order;
    }
}