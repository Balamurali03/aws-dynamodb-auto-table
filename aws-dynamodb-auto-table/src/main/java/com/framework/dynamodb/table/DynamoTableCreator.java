package com.framework.dynamodb.table;

import com.framework.dynamodb.schema.DynamoGsiDefinition;
import com.framework.dynamodb.schema.DynamoTableSchema;


import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

public class DynamoTableCreator {

    private final DynamoDbClient dynamoDbClient;

    public DynamoTableCreator(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void createTableIfNotExists(DynamoTableSchema schema) {

        if (tableExists(schema.tableName())) {
            return;
        }

        CreateTableRequest request = buildCreateTableRequest(schema);
        dynamoDbClient.createTable(request);
    }

    private boolean tableExists(String tableName) {
        try {
            dynamoDbClient.describeTable(
                    DescribeTableRequest.builder()
                            .tableName(tableName)
                            .build()
            );
            return true;
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }

    private CreateTableRequest buildCreateTableRequest(DynamoTableSchema schema) {

        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        List<KeySchemaElement> keySchema = new ArrayList<>();

        // Primary partition key
        attributeDefinitions.add(
                AttributeDefinition.builder()
                        .attributeName(schema.partitionKey())
                        .attributeType(ScalarAttributeType.S)
                        .build()
        );

        keySchema.add(
                KeySchemaElement.builder()
                        .attributeName(schema.partitionKey())
                        .keyType(KeyType.HASH)
                        .build()
        );

        // Optional sort key
        if (schema.sortKey() != null) {
            attributeDefinitions.add(
                    AttributeDefinition.builder()
                            .attributeName(schema.sortKey())
                            .attributeType(ScalarAttributeType.S)
                            .build()
            );

            keySchema.add(
                    KeySchemaElement.builder()
                            .attributeName(schema.sortKey())
                            .keyType(KeyType.RANGE)
                            .build()
            );
        }

        // GSIs
        List<GlobalSecondaryIndex> globalSecondaryIndexes =
                buildGlobalSecondaryIndexes(schema, attributeDefinitions);

        return CreateTableRequest.builder()
                .tableName(schema.tableName())
                .attributeDefinitions(attributeDefinitions)
                .keySchema(keySchema)
                .billingMode(BillingMode.PAY_PER_REQUEST) // AWS-recommended default
                .globalSecondaryIndexes(globalSecondaryIndexes.isEmpty() ? null : globalSecondaryIndexes)
                .build();
    }

    private List<GlobalSecondaryIndex> buildGlobalSecondaryIndexes(
        DynamoTableSchema schema,
        List<AttributeDefinition> attributeDefinitions
) {

    List<GlobalSecondaryIndex> awsGsis = new ArrayList<>();

    for (DynamoGsiDefinition gsi : schema.globalSecondaryIndexes().values()) {

        attributeDefinitions.add(
                AttributeDefinition.builder()
                        .attributeName(gsi.partitionKey())
                        .attributeType(ScalarAttributeType.S)
                        .build()
        );

        List<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(
                KeySchemaElement.builder()
                        .attributeName(gsi.partitionKey())
                        .keyType(KeyType.HASH)
                        .build()
        );

        if (gsi.sortKey() != null) {
            attributeDefinitions.add(
                    AttributeDefinition.builder()
                            .attributeName(gsi.sortKey())
                            .attributeType(ScalarAttributeType.S)
                            .build()
            );

            keySchema.add(
                    KeySchemaElement.builder()
                            .attributeName(gsi.sortKey())
                            .keyType(KeyType.RANGE)
                            .build()
            );
        }

        awsGsis.add(
                GlobalSecondaryIndex.builder()
                        .indexName(gsi.indexName())
                        .keySchema(keySchema)
                        .projection(
                                Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build()
                        )
                        .build()
        );
    }

    return awsGsis;
}
}
