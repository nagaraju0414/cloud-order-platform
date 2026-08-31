/*package com.cloudorder.inventory.cosumer;

import com.cloudorder.inventory.event.OrderCreatedEvent;
import com.cloudorder.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {
    private final InventoryService inventoryService;
    @KafkaListener(
           topics = "order_created",
           groupId = "inventory-group"
    )
    public void consume(OrderCreatedEvent orderCreatedEvent) {
       inventoryService.processOrder(orderCreatedEvent);
    }
}*/
