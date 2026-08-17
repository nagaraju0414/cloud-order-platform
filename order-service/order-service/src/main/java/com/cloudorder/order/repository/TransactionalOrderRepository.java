package com.cloudorder.order.repository;

import com.cloudorder.order.domain.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TransactionalOrderRepository {

    private final DynamoDbClient dynamoDbClient;

    private final String orderTable;
    private final String outboxTable;

    public TransactionalOrderRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${dynamodb.table-name}") String orderTable,
            @Value("${dynamodb.outbox-table-name}") String outboxTable) {

        this.dynamoDbClient = dynamoDbClient;
        this.orderTable = orderTable;
        this.outboxTable = outboxTable;
    }

    public void save(
            Order order,
            OutboxItem outbox) {

        Map<String, AttributeValue> orderItem =
                createOrderItem(order);

        Map<String, AttributeValue> outboxItem =
                createOutboxItem(outbox);

        TransactWriteItem orderWrite =
                TransactWriteItem.builder()
                        .put(
                                Put.builder()
                                        .tableName(orderTable)
                                        .item(orderItem)
                                        .build()
                        )
                        .build();

        TransactWriteItem outboxWrite =
                TransactWriteItem.builder()
                        .put(
                                Put.builder()
                                        .tableName(outboxTable)
                                        .item(outboxItem)
                                        .build()
                        )
                        .build();

        dynamoDbClient.transactWriteItems(
                TransactWriteItemsRequest.builder()
                        .transactItems(
                                orderWrite,
                                outboxWrite
                        )
                        .build()
        );
    }

    private Map<String, AttributeValue> createOrderItem(
            Order order) {

        Map<String, AttributeValue> item =
                new HashMap<>();

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
                "status",
                AttributeValue.builder()
                        .s(order.status().name())
                        .build()
        );

        item.put(
                "totalAmount",
                AttributeValue.builder()
                        .n(order.totalAmount().toString())
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

    private Map<String, AttributeValue> createOutboxItem(
            OutboxItem outbox) {

        Map<String, AttributeValue> item =
                new HashMap<>();

        item.put(
                "id",
                AttributeValue.builder()
                        .s(outbox.getEventId())
                        .build()
        );

        item.put(
                "eventId",
                AttributeValue.builder()
                        .s(outbox.getEventId())
                        .build()
        );

        item.put(
                "eventType",
                AttributeValue.builder()
                        .s(outbox.getEventType())
                        .build()
        );

        item.put(
                "aggregateType",
                AttributeValue.builder()
                        .s(outbox.getAggregateType())
                        .build()
        );

        item.put(
                "aggregateId",
                AttributeValue.builder()
                        .s(outbox.getAggregateId())
                        .build()
        );

        item.put(
                "payload",
                AttributeValue.builder()
                        .s(outbox.getPayload())
                        .build()
        );

        item.put(
                "status",
                AttributeValue.builder()
                        .s(outbox.getStatus())
                        .build()
        );

        item.put(
                "retryCount",
                AttributeValue.builder()
                        .n(
                                String.valueOf(
                                        outbox.getRetryCount()
                                )
                        )
                        .build()
        );

        item.put(
                "createdAt",
                AttributeValue.builder()
                        .s(outbox.getCreatedAt())
                        .build()
        );

        return item;
    }

    public Optional<Order> findById(String orderId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(orderTable)
                .keyConditionExpression("PK = :pk AND SK = :sk")
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s("ORDER#" + orderId).build(),
                        ":sk", AttributeValue.builder().s("ORDER#" + orderId).build()
                ))
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        if (response.items().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToOrder(response.items().get(0)));
    }

    public List<Order> findByCustomerId(String customerId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(orderTable)
                .indexName("GSI1")
                .keyConditionExpression("GSI1PK = :pk")
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s("CUSTOMER#" + customerId).build()
                ))
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        return response.items().stream()
                .map(this::mapToOrder)
                .toList();
    }

    public Order update(Order order) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("PK", AttributeValue.builder().s("ORDER#" + order.orderId()).build());
        key.put("SK", AttributeValue.builder().s("ORDER#" + order.orderId()).build());

        Map<String, AttributeValueUpdate> updates = new HashMap<>();
        updates.put("status", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(order.status().name()).build())
                .action(AttributeAction.PUT)
                .build());
        updates.put("updatedAt", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(order.updatedAt().toString()).build())
                .action(AttributeAction.PUT)
                .build());

        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(orderTable)
                .key(key)
                .attributeUpdates(updates)
                .build());

        return order;
    }

    private Order mapToOrder(Map<String, AttributeValue> item) {
        return new Order(
                item.get("orderId").s(),
                item.get("customerId").s(),
                List.of(),
                new BigDecimal(item.get("totalAmount").n()),
                com.cloudorder.order.domain.OrderStatus.valueOf(item.get("status").s()),
                Instant.parse(item.get("createdAt").s()),
                Instant.parse(item.get("updatedAt").s())
        );
    }
}