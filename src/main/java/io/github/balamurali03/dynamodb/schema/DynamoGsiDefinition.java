package io.github.balamurali03.dynamodb.schema;

import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;

public class DynamoGsiDefinition {

    private final String indexName;
    private String partitionKey;
    private String sortKey;
    private List<String> projectedAttributes;

    public DynamoGsiDefinition(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public List<String> getProjectedAttributes() {
        return projectedAttributes;
    }

    public void setProjectedAttributes(List<String> projectedAttributes) {
        this.projectedAttributes = projectedAttributes;
    }

    public GlobalSecondaryIndex toAwsGsi(long read, long write) {

        if (partitionKey == null) {
            throw new IllegalStateException(
                    "GSI '" + indexName + "' must have a partition key");
        }

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

        Projection.Builder projection = Projection.builder();

        if (projectedAttributes == null || projectedAttributes.isEmpty()) {
            projection.projectionType(ProjectionType.ALL);
        } else {
            projection.projectionType(ProjectionType.INCLUDE)
                      .nonKeyAttributes(projectedAttributes);
        }

        return GlobalSecondaryIndex.builder()
                .indexName(indexName)
                .keySchema(keySchema)
                .projection(projection.build())
                .provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(read)
                                .writeCapacityUnits(write)
                                .build())
                .build();
    }
}
