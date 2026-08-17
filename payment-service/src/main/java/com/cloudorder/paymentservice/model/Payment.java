package com.cloudorder.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Payment {

    private String paymentId;

    private String orderId;

    private String customerId;

    private BigDecimal amount;

    private PaymentStatus status;

    private String idempotencyKey;

    private String createdAt;

    private String updatedAt;

    @DynamoDbPartitionKey
    public String getPaymentId() {
        return paymentId;
    }
}