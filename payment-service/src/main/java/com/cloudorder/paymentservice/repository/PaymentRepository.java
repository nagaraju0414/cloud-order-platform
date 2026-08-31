package com.cloudorder.paymentservice.repository;
import com.cloudorder.paymentservice.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Repository
public class PaymentRepository {

    private final DynamoDbTable<Payment> table;

    public PaymentRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${dynamodb.table-name}") String tableName) {

        this.table = enhancedClient.table(
                tableName,
                TableSchema.fromBean(Payment.class)
        );
    }

    public void save(Payment payment) {
        table.putItem(payment);
    }

    public Optional<Payment> findById(String paymentId) {

        Payment payment = table.getItem(
                Key.builder()
                        .partitionValue(paymentId)
                        .build()
        );

        return Optional.ofNullable(payment);
    }
}