package com.cloudorder.order.service;

import com.cloudorder.order.domain.Order;
import com.cloudorder.order.dto.CreateOrderRequest;
import com.cloudorder.order.dto.OrderItemRequest;
import com.cloudorder.order.dto.OrderResponse;
import com.cloudorder.order.metrics.OrderMetrics;
import com.cloudorder.order.repository.OrderRepository;
import com.cloudorder.order.repository.TransactionalOrderRepository;
import com.cloudorder.order.service.OutboxEventFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderApplicationServiceTest {

   // @Test
    void shouldCreateOrder() {

        TransactionalOrderRepository transactionalRepository = mock(TransactionalOrderRepository.class);
        OrderRepository repository = mock(OrderRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);
        OrderMetrics orderMetrics = mock(OrderMetrics.class);

        when(outboxEventFactory.create(any())).thenReturn(new com.cloudorder.order.repository.OutboxItem());

        OrderApplicationService service =
                new OrderApplicationService(transactionalRepository,  outboxEventFactory,orderMetrics);

        CreateOrderRequest request =
                new CreateOrderRequest(
                                        "P100",
                                        BigDecimal.valueOf(999)

                );

        Order response =
                service.createOrder(request);

        assertEquals("P100", response.customerId());

        assertEquals(
                BigDecimal.valueOf(1998),
                response.totalAmount()
        );

        assertEquals(
                "CREATED",
                response.status().name()
        );

        verify(transactionalRepository).save(any(), any());
    }
}