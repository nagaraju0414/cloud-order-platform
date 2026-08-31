package com.cloudorder.order.repository;

import com.cloudorder.order.domain.Order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
/*import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OrderRepository {

    private final DynamoDbTable<Map<String, AttributeValue>> table;

    public OrderRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${dynamodb.table-name}")
            String tableName) {

        this.table =
                enhancedClient.table(
                        tableName,
                        TableSchema.mapSchema()
                );
    }

    public Map<String, AttributeValue> toItem(
            Order order) {

        Map<String, AttributeValue> item =
                new HashMap<>();

        item.put(
                "PK",
                AttributeValue.builder()
                        .s("ORDER#" + order.orderId())
                        .build()
        );

        item.put(
                "SK",
                AttributeValue.builder()
                        .s("ORDER#" + order.orderId())
                        .build()
        );

        item.put(
                "entityType",
                AttributeValue.builder()
                        .s("ORDER")
                        .build()
        );

        item.put(
                "orderId",
                AttributeValue.builder()
                        .s(order.orderId())
                        .build()
        );

        item.put(
                "customerId",
                AttributeValue.builder()
                        .s(order.customerId())
                        .build()
        );

        item.put(
                "totalAmount",
                AttributeValue.builder()
                        .n(order.totalAmount().toString())
                        .build()
        );

        item.put(
                "status",
                AttributeValue.builder()
                        .s(order.status().name())
                        .build()
        );

        item.put(
                "createdAt",
                AttributeValue.builder()
                        .s(order.createdAt().toString())
                        .build()
        );

        item.put(
                "updatedAt",
                AttributeValue.builder()
                        .s(order.updatedAt().toString())
                        .build()
        );

        return item;
    }
}*/