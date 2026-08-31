package com.cloudorder.order.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.Instant;

@Repository
public class OutboxRepository {

    private final DynamoDbTable<OutboxItem> table;

    public OutboxRepository(

            DynamoDbEnhancedClient enhancedClient,

            @Value("${dynamodb.outbox-table-name}")
            String tableName) {

        this.table =
                enhancedClient.table(
                        tableName,
                        TableSchema.fromBean(
                                OutboxItem.class
                        )
                );
    }

    public Iterable<OutboxItem>
    findPending() {

        QueryConditional condition =
                QueryConditional.keyEqualTo(

                        Key.builder()

                                .partitionValue(
                                        "OUTBOX#PENDING"
                                )

                                .build()
                );

        QueryEnhancedRequest request =
                QueryEnhancedRequest.builder()

                        .queryConditional(
                                condition
                        )

                        .limit(50)

                        .build();

        return table
                .index("GSI1")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .toList();
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

    public void markFailed(
            OutboxItem item) {

        item.setStatus(
                OutboxStatus.FAILED.name()
        );

        item.setGsi1pk(null);

        item.setGsi1sk(null);

        table.updateItem(item);
    }

    public void updateRetry(
            OutboxItem item) {

        table.updateItem(item);
    }
}