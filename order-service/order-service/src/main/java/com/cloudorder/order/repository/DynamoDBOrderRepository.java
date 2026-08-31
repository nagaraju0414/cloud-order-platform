package com.cloudorder.order.repository;

import com.cloudorder.order.domain.Order;
import com.cloudorder.order.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
/*import software.amazon.awssdk.enhanced.dynamodb.*;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DynamoDBOrderRepository implements OrderRepository {

    private final DynamoDbTable<Order> table;

    public DynamoDBOrderRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${dynamodb.table-name}")
            String tableName) {

        this.table =
                enhancedClient.table(
                        tableName,
                        TableSchema.fromBean(Order.class)
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

    @Override
    public Order save(Order order) {
        return null;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.empty();
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return List.of();
    }
}
*/
