package com.cloudorder.order.repository;

import com.cloudorder.order.domain.Order;
import com.cloudorder.order.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class DynamoDBOrderRepository implements OrderRepository {

    private final DynamoDbTable<DynamoOrderItem> table;

    public DynamoDBOrderRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${dynamodb.table-name}") String tableName) {

        this.table = enhancedClient.table(
                tableName,
                TableSchema.fromBean(DynamoOrderItem.class)
        );
    }

    @Override
    public Order save(Order order) {

        DynamoOrderItem item = toDynamo(order);

        table.putItem(item);

        return order;
    }

    @Override
    public Optional<Order> findById(String orderId) {

        Key key = Key.builder()
                .partitionValue(orderId)
                .build();

        DynamoOrderItem item = table.getItem(
                request -> request.key(key)
        );

        return Optional.ofNullable(item)
                .map(this::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        // GSI not available - return empty list for now
        return List.of();
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
                List.of(),
                item.getTotalAmount(),
                OrderStatus.valueOf(item.getStatus()),
                Instant.parse(item.getCreatedAt()),
                Instant.parse(item.getUpdatedAt())
        );
    }
}