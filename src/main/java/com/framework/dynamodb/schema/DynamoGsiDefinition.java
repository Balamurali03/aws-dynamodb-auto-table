package com.framework.dynamodb.schema;

import java.util.List;

import software.amazon.awssdk.services.dynamodb.model.*;

public record DynamoGsiDefinition(
        String indexName,
        String partitionKey,
        String sortKey,
        List<String> projectedAttributes
) {

    public GlobalSecondaryIndex toAwsGsi(long read, long write) {

        var keySchema = new java.util.ArrayList<KeySchemaElement>();

        keySchema.add(KeySchemaElement.builder()
                .attributeName(partitionKey)
                .keyType(KeyType.HASH)
                .build());

        if (sortKey != null) {
            keySchema.add(KeySchemaElement.builder()
                    .attributeName(sortKey)
                    .keyType(KeyType.RANGE)
                    .build());
        }

        return GlobalSecondaryIndex.builder()
                .indexName(indexName)
                .keySchema(keySchema)
                .projection(Projection.builder()
                        .projectionType(ProjectionType.ALL)
                        .build())
                .provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(read)
                                .writeCapacityUnits(write)
                                .build())
                .build();
    }
}
