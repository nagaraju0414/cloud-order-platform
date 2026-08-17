package com.cloudorder.order.service;

import com.cloudorder.order.domain.Order;
import com.cloudorder.order.domain.OrderItem;
import com.cloudorder.order.domain.OrderStatus;
import com.cloudorder.order.dto.CreateOrderRequest;
import com.cloudorder.order.dto.OrderItemRequest;
import com.cloudorder.order.dto.OrderResponse;
import com.cloudorder.order.event.OrderCreatedEvent;
import com.cloudorder.order.event.OrderEventPublisher;
import com.cloudorder.order.exception.OrderNotFoundException;
import com.cloudorder.order.repository.OrderRepository;
import com.cloudorder.order.repository.OutboxItem;
import com.cloudorder.order.repository.TransactionalOrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderApplicationService {

    private final TransactionalOrderRepository transactionalRepository;
    private final OrderRepository orderRepository;
    private final OutboxEventFactory outboxEventFactory;

    public OrderApplicationService(
            TransactionalOrderRepository transactionalRepository,
            OrderRepository orderRepository,
            OutboxEventFactory outboxEventFactory) {

        this.transactionalRepository = transactionalRepository;
        this.orderRepository = orderRepository;
        this.outboxEventFactory =
                outboxEventFactory;
    }

    public OrderResponse createOrder(
            CreateOrderRequest request) {

        Order order = createOrderFromRequest(request);

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        UUID.randomUUID().toString(),
                        "ORDER_CREATED",
                        order.orderId(),
                        order.customerId(),
                        order.totalAmount(),
                        Instant.now()
                );

        OutboxItem outbox =
                outboxEventFactory.create(event);

        transactionalRepository.save(order, outbox);

        return toOrderResponse(order);
    }

    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return toOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    public OrderResponse cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        Order cancelledOrder = new Order(
                order.orderId(),
                order.customerId(),
                order.items(),
                order.totalAmount(),
                OrderStatus.CANCELLED,
                order.createdAt(),
                Instant.now()
        );
        
        orderRepository.save(cancelledOrder);
        return toOrderResponse(cancelledOrder);
    }

    private Order createOrderFromRequest(CreateOrderRequest request) {
        List<OrderItem> orderItems = request.items().stream()
                .map(item -> new OrderItem(item.productId(), item.quantity(), item.unitPrice()))
                .toList();

        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Instant now = Instant.now();

        return new Order(
                UUID.randomUUID().toString(),
                request.customerId(),
                orderItems,
                totalAmount,
                OrderStatus.CREATED,
                now,
                now
        );
    }

    private OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.orderId(),
                order.customerId(),
                order.totalAmount(),
                order.status(),
                order.createdAt(),
                order.updatedAt()
        );
    }
}
