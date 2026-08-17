package com.cloudorder.inventory.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InventoryRepository {

    private final DynamoDbClient dynamoDbClient;

    private final String tableName;

    public InventoryRepository(

            DynamoDbClient dynamoDbClient,

            @Value("${dynamodb.table-name}")
            String tableName) {

        this.dynamoDbClient =
                dynamoDbClient;

        this.tableName =
                tableName;
    }

    public void create(

            String productId,

            String productName,

            int quantity) {

        Map<String, AttributeValue> item =
                new HashMap<>();

        item.put(
                "PK",
                AttributeValue.builder()
                        .s("PRODUCT#" + productId)
                        .build()
        );

        item.put(
                "productId",
                AttributeValue.builder()
                        .s(productId)
                        .build()
        );

        item.put(
                "productName",
                AttributeValue.builder()
                        .s(productName)
                        .build()
        );

        item.put(
                "availableQuantity",
                AttributeValue.builder()
                        .n(String.valueOf(quantity))
                        .build()
        );

        item.put(
                "reservedQuantity",
                AttributeValue.builder()
                        .n("0")
                        .build()
        );

        dynamoDbClient.putItem(

                PutItemRequest.builder()

                        .tableName(tableName)

                        .item(item)

                        .build()
        );
    }

    public boolean reserve(

            String productId,

            int quantity) {

        try {

            Map<String, AttributeValue>
                    values =
                    new HashMap<>();

            values.put(
                    ":quantity",
                    AttributeValue.builder()
                            .n(String.valueOf(quantity))
                            .build()
            );

            UpdateItemRequest request =
                    UpdateItemRequest.builder()

                            .tableName(tableName)

                            .key(
                                    Map.of(
                                            "PK",
                                            AttributeValue.builder()
                                                    .s(
                                                            "PRODUCT#" +
                                                                    productId
                                                    )
                                                    .build()
                                    )
                            )

                            .updateExpression(
                                    "SET " +
                                            "availableQuantity = " +
                                            "availableQuantity - :quantity, " +

                                            "reservedQuantity = " +
                                            "reservedQuantity + :quantity"
                            )

                            .conditionExpression(
                                    "availableQuantity >= :quantity"
                            )

                            .expressionAttributeValues(
                                    values
                            )

                            .build();

            dynamoDbClient.updateItem(request);

            return true;

        } catch (
                ConditionalCheckFailedException e) {

            return false;
        }
    }
}