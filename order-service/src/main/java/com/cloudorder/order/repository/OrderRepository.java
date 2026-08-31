package com.cloudorder.order.repository;

import com.cloudorder.order.domain.Order;

import com.cloudorder.order.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Repository
public class OrderRepository {

    private final DynamoDbTable<DynamoOrderItem> table;

    public OrderRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${dynamodb.table-name}")
            String tableName) {

        this.table =
                enhancedClient.table(
                        tableName,
                        TableSchema.fromBean(DynamoOrderItem.class)
                );
    }

    private DynamoOrderItem toDynamo(Order order) {

        DynamoOrderItem item = new DynamoOrderItem();

        item.setOrderId(order.orderId());
        item.setCustomerId(order.customerId());

        item.setStatus(order.status().name());

        item.setTotalAmount(order.totalAmount());

        item.setCreatedAt(order.createdAt().toString());
        item.setUpdatedAt(order.updatedAt().toString());

        return item;
    }

    private Order toDomain(DynamoOrderItem item) {

        return new Order(
                item.getOrderId(),
                item.getCustomerId(),
                item.getTotalAmount(),
                OrderStatus.valueOf(item.getStatus()),
                Instant.parse(item.getCreatedAt()),
                Instant.parse(item.getUpdatedAt())
        );
    }
}