package com.cloudorder.order.repository;

import com.cloudorder.order.domain.Order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Put;

import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

import java.util.HashMap;
import java.util.Map;

@Repository
public class TransactionalOrderRepository {

    private final DynamoDbClient dynamoDbClient;

    private final String orderTable;

    private final String outboxTable;

    public TransactionalOrderRepository(

            DynamoDbClient dynamoDbClient,

            @Value("${dynamodb.table-name}")
            String orderTable,

            @Value("${dynamodb.outbox-table-name}")
            String outboxTable) {

        this.dynamoDbClient =
                dynamoDbClient;

        this.orderTable =
                orderTable;

        this.outboxTable =
                outboxTable;
    }

    public void save(
            Order order,
            OutboxItem outbox) {

        Map<String, AttributeValue>
                orderItem =
                createOrderItem(order);

        Map<String, AttributeValue>
                outboxItem =
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

        TransactWriteItemsRequest request =
                TransactWriteItemsRequest.builder()

                        .transactItems(
                                orderWrite,
                                outboxWrite
                        )

                        .build();

        dynamoDbClient
                .transactWriteItems(request);
    }

    private Map<String, AttributeValue>
    createOrderItem(Order order) {

        Map<String, AttributeValue>
                item = new HashMap<>();

        item.put(
                "PK",
                AttributeValue.builder()
                        .s(
                                "ORDER#" +
                                        order.orderId()
                        )
                        .build()
        );

        item.put(
                "SK",
                AttributeValue.builder()
                        .s(
                                "ORDER#" +
                                        order.orderId()
                        )
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
                        .n(
                                order.totalAmount()
                                        .toString()
                        )
                        .build()
        );

        item.put(
                "status",
                AttributeValue.builder()
                        .s(
                                order.status()
                                        .name()
                        )
                        .build()
        );

        item.put(
                "createdAt",
                AttributeValue.builder()
                        .s(
                                order.createdAt()
                                        .toString()
                        )
                        .build()
        );

        item.put(
                "updatedAt",
                AttributeValue.builder()
                        .s(
                                order.updatedAt()
                                        .toString()
                        )
                        .build()
        );

        return item;
    }

    private Map<String, AttributeValue>
    createOutboxItem(
            OutboxItem outbox) {

        Map<String, AttributeValue>
                item = new HashMap<>();

        item.put(
                "PK",
                AttributeValue.builder()
                        .s(outbox.getPk())
                        .build()
        );

        item.put(
                "SK",
                AttributeValue.builder()
                        .s(outbox.getSk())
                        .build()
        );

        item.put(
                "GSI1PK",
                AttributeValue.builder()
                        .s(outbox.getGsi1pk())
                        .build()
        );

        item.put(
                "GSI1SK",
                AttributeValue.builder()
                        .s(outbox.getGsi1sk())
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

        item.put(
                "nextAttemptAt",
                AttributeValue.builder()
                        .s(
                                outbox.getNextAttemptAt()
                        )
                        .build()
        );

        return item;
    }
}