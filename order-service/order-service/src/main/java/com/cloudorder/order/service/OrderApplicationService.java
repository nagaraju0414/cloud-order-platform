package com.cloudorder.order.service;

import com.cloudorder.order.domain.Order;
import com.cloudorder.order.domain.OrderStatus;

import com.cloudorder.order.dto.CreateOrderRequest;

import com.cloudorder.order.event.OrderCreatedEvent;

import com.cloudorder.order.metrics.OrderMetrics;
import com.cloudorder.order.repository.OutboxItem;
import com.cloudorder.order.repository.TransactionalOrderRepository;

import io.micrometer.core.instrument.Timer;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderApplicationService {

    private final TransactionalOrderRepository
            repository;

    private final OutboxEventFactory
            outboxEventFactory;
    private final OrderMetrics orderMetrics;

    public OrderApplicationService(

            TransactionalOrderRepository repository,

            OutboxEventFactory outboxEventFactory,OrderMetrics orderMetrics) {

        this.repository =
                repository;

        this.outboxEventFactory =
                outboxEventFactory;
        this.orderMetrics =
                orderMetrics;
    }

    public Order createOrder(
            CreateOrderRequest request) {

        Instant now =
                Instant.now();
        Timer.Sample sample = orderMetrics.startTimer();
        try {
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

                            now,
                            MDC.get("X-Correlation-ID")
                    );

            OutboxItem outbox =
                    outboxEventFactory
                            .create(event);

            repository.save(
                    order,
                    outbox
            );
            orderMetrics.incrementOrdersCreated();
            return order;
        } catch (Exception ex) {

            orderMetrics.incrementOrdersFailed();

            throw ex;

        } finally {

            orderMetrics.recordProcessingTime(sample);
        }
    }
}