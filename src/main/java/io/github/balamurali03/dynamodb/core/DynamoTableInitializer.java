package io.github.balamurali03.dynamodb.core;

import io.github.balamurali03.dynamodb.annotation.DynamoEntity;
import io.github.balamurali03.dynamodb.schema.DynamoSchemaExtractor;
import io.github.balamurali03.dynamodb.schema.DynamoTableSchema;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DynamoTableInitializer {

    private final DynamoDbClient client;

    public DynamoTableInitializer(DynamoDbClient client) {
        this.client = client;
    }

    public void initialize(Class<?> entityClass) {

        DynamoEntity entity =
                entityClass.getAnnotation(DynamoEntity.class);

        if (entity == null || !entity.autoCreate()) {
            return;
        }

        String tableName = entity.tableName().isBlank()
                ? entityClass.getSimpleName()
                : entity.tableName();

        try {
            client.describeTable(r -> r.tableName(tableName));
            log.debug("DynamoDB table already exists: {}", tableName);
            return;
        } catch (ResourceNotFoundException ignored) {
            // Table does not exist → create
        }

        try {
            DynamoTableSchema schema =
                    DynamoSchemaExtractor.extract(entityClass);

            List<AttributeDefinition> attributes = new ArrayList<>();
            schema.attributes().forEach((name, type) ->
                    attributes.add(AttributeDefinition.builder()
                            .attributeName(name)
                            .attributeType(ScalarAttributeType.fromValue(type))
                            .build())
            );

            List<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(KeySchemaElement.builder()
                    .attributeName(schema.partitionKey())
                    .keyType(KeyType.HASH)
                    .build());

            if (schema.sortKey() != null) {
                keySchema.add(KeySchemaElement.builder()
                        .attributeName(schema.sortKey())
                        .keyType(KeyType.RANGE)
                        .build());
            }

            CreateTableRequest.Builder builder =
                    CreateTableRequest.builder()
                            .tableName(tableName)
                            .attributeDefinitions(attributes)
                            .keySchema(keySchema);

            if (!schema.globalSecondaryIndexes().isEmpty()) {
                List<GlobalSecondaryIndex> gsis = new ArrayList<>();
                schema.globalSecondaryIndexes().values()
                        .forEach(gsi ->
                                gsis.add(gsi.toAwsGsi(
                                        entity.readCapacity(),
                                        entity.writeCapacity()))
                        );
                builder.globalSecondaryIndexes(gsis);
            }

            if (entity.billingMode().isProvisioned()) {
                builder.provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(entity.readCapacity())
                                .writeCapacityUnits(entity.writeCapacity())
                                .build());
            } else {
                builder.billingMode(BillingMode.PAY_PER_REQUEST);
            }

            client.createTable(builder.build());
            log.info("✅ DynamoDB table created successfully: {}", tableName);

        } catch (Exception ex) {
            log.error(
                    "❌ Failed to create DynamoDB table '{}' for entity {}",
                    tableName,
                    entityClass.getName(),
                    ex
            );
        }
    }
}
