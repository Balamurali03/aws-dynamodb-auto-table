package com.framework.dynamodb.schema;

import java.util.Map;

public record DynamoTableSchema(
        String tableName,
        String partitionKey,
        String sortKey,
        Map<String, String> attributes,
        Map<String, DynamoGsiDefinition> globalSecondaryIndexes
) {
    public boolean hasGsi() {
        return globalSecondaryIndexes != null && !globalSecondaryIndexes.isEmpty();
    }
}
