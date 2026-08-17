package com.cloudorder.order.repository;

import software.amazon.awssdk.enhanced.dynamodb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;

@Repository
public class OutboxRepository {

    private final DynamoDbTable<OutboxItem> table;

    public OutboxRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${dynamodb.outbox-table-name}") String tableName) {

        this.table = enhancedClient.table(
                tableName,
                TableSchema.fromBean(OutboxItem.class)
        );
    }

    public void save(OutboxItem item) {
        table.putItem(item);
    }

    public DynamoDbTable<OutboxItem> table() {
        return table;
    }
    public PageIterable<OutboxItem> findPending() {

        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(
                                        "OUTBOX#PENDING"
                                )
                                .build()
                );

        return (PageIterable<OutboxItem>) table.index("GSI1")
                .query(
                        QueryEnhancedRequest.builder()
                                .queryConditional(condition)
                                .limit(50)
                                .build()
                );
    }

    public void markPublished(
            OutboxItem item) {

        item.setStatus(
                OutboxStatus.PUBLISHED.name()
        );

        item.setPublishedAt(
                Instant.now().toString()
        );

        item.setGsi1pk(null);
        item.setGsi1sk(null);

        table.updateItem(item);
    }

    public void markFailed(OutboxItem item) {
        item.setStatus(OutboxStatus.FAILED.name());
        item.setGsi1pk(null);
        item.setGsi1sk(null);
        table.updateItem(item);
    }

    public void updateRetry(OutboxItem item) {
        table.updateItem(item);
    }
}