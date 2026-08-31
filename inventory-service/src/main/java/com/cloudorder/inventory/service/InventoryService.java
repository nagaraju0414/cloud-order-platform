package com.cloudorder.inventory.service;

import com.cloudorder.inventory.domain.InventoryItem;
import com.cloudorder.inventory.dto.InventoryResponse;
import com.cloudorder.inventory.event.OrderCreatedEvent;
import com.cloudorder.inventory.repository.InventoryRepository;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.Map;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(
            InventoryRepository repository) {

        this.repository = repository;
    }

    public void createInventory(

            String productId,

            String productName,

            int quantity) {

        repository.create(
                productId,
                productName,
                quantity
        );
    }

    public boolean reserve(

            String productId,

            int quantity) {
        return false;
    }

    public void processOrder(OrderCreatedEvent orderCreatedEvent) {
    }

    public InventoryResponse checkInventory(String productId) {
     return processInventoryItem(repository.getInventoryByProductId(productId));
      
    }
    public InventoryResponse processInventoryItem(Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            throw new RuntimeException("Product not found in inventory table");
        }

        String productId = item.get("productId").s();
        String productName = item.get("productName").s();
        int availableQuantity = Integer.parseInt(item.get("availableQuantity").n());
        int reservedQuantity = Integer.parseInt(item.get("reservedQuantity").n());
        Instant updatedAt = item.get("updatedAt") != null 
                ? Instant.parse(item.get("updatedAt").s()) 
                : Instant.now();

        InventoryItem inventoryItem = new InventoryItem(
                productId,
                productName,
                availableQuantity,
                reservedQuantity,
                updatedAt
        );

       return InventoryResponse.from(inventoryItem);
    }

}