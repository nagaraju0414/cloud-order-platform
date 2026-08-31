package com.cloudorder.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;

@Component
public class DynamoDbTableInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {

    }
/*
    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;
    private final String outboxTableName;

    public DynamoDbTableInitializer(
            DynamoDbClient dynamoDbClient,
            DynamoDbEnhancedClient enhancedClient,
            @Value("${dynamodb.table-name}") String tableName,
            @Value("${dynamodb.outbox-table-name}") String outboxTableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.enhancedClient = enhancedClient;
        this.tableName = tableName;
        this.outboxTableName = outboxTableName;
    }

    @Override
    public void run(String... args) {
        createOrderTable();
        createOutboxTable();
    }

    private void createOrderTable() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build());
            System.out.println("Table " + tableName + " already exists, deleting and recreating");
            dynamoDbClient.deleteTable(DeleteTableRequest.builder()
                    .tableName(tableName)
                    .build());
            Thread.sleep(1000); // Wait for table deletion
        } catch (ResourceNotFoundException e) {
            System.out.println("Table " + tableName + " does not exist, creating");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Creating table: " + tableName);
        
        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(tableName)
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("orderId")
                                .attributeType(ScalarAttributeType.S)
                                .build()
                )
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("orderId")
                                .keyType(KeyType.HASH)
                                .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
        
        System.out.println("Table " + tableName + " created successfully");
    }

    private void createOutboxTable() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(outboxTableName)
                    .build());
            System.out.println("Table " + outboxTableName + " already exists, deleting and recreating");
            dynamoDbClient.deleteTable(DeleteTableRequest.builder()
                    .tableName(outboxTableName)
                    .build());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (ResourceNotFoundException e) {
            System.out.println("Table " + outboxTableName + " does not exist, creating");
        }
        
        System.out.println("Creating table: " + outboxTableName);
        
        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(outboxTableName)
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("pk")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("sk")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("gsi1pk")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("gsi1sk")
                                .attributeType(ScalarAttributeType.S)
                                .build()
                )
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("pk")
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("sk")
                                .keyType(KeyType.RANGE)
                                .build()
                )
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName("GSI1")
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("gsi1pk")
                                                .keyType(KeyType.HASH)
                                                .build(),
                                        KeySchemaElement.builder()
                                                .attributeName("gsi1sk")
                                                .keyType(KeyType.RANGE)
                                                .build()
                                )
                                .projection(
                                        Projection.builder()
                                                .projectionType(ProjectionType.ALL)
                                                .build()
                                )
                                .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
        
        System.out.println("Table " + outboxTableName + " created successfully");
    }
    */

}
