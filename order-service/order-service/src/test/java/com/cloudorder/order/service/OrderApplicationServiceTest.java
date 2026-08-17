package com.cloudorder.order.service;

import com.cloudorder.order.dto.CreateOrderRequest;
import com.cloudorder.order.dto.OrderItemRequest;
import com.cloudorder.order.dto.OrderResponse;
import com.cloudorder.order.repository.OrderRepository;
import com.cloudorder.order.repository.TransactionalOrderRepository;
import com.cloudorder.order.service.OutboxEventFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderApplicationServiceTest {

    @Test
    void shouldCreateOrder() {

        TransactionalOrderRepository transactionalRepository = mock(TransactionalOrderRepository.class);
        OrderRepository repository = mock(OrderRepository.class);
        OutboxEventFactory outboxEventFactory = mock(OutboxEventFactory.class);

        when(outboxEventFactory.create(any())).thenReturn(new com.cloudorder.order.repository.OutboxItem());

        OrderApplicationService service =
                new OrderApplicationService(transactionalRepository, repository, outboxEventFactory);

        CreateOrderRequest request =
                new CreateOrderRequest(
                        "C1001",
                        List.of(
                                new OrderItemRequest(
                                        "P100",
                                        2,
                                        BigDecimal.valueOf(999)
                                )
                        )
                );

        OrderResponse response =
                service.createOrder(request);

        assertEquals("C1001", response.customerId());

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