package io.github.balamurali03.dynamodb.core;

import io.github.balamurali03.dynamodb.annotation.DynamoEntity;
import io.github.balamurali03.dynamodb.schema.DynamoGsiDefinition;
import io.github.balamurali03.dynamodb.schema.DynamoSchemaExtractor;
import io.github.balamurali03.dynamodb.schema.DynamoTableSchema;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Slf4j
public class DynamoTableInitializer {

    private final DynamoDbClient client;

    public DynamoTableInitializer(DynamoDbClient client) {
        this.client = client;
    }

    public void initialize(Class<?> entityClass) {

        DynamoEntity entity = entityClass.getAnnotation(DynamoEntity.class);
        if (entity == null || !entity.autoCreate()) {
            return;
        }

        String tableName = entity.tableName().isBlank()
                ? entityClass.getSimpleName()
                : entity.tableName();

        // 1️⃣ Check if table already exists
        try {
            client.describeTable(r -> r.tableName(tableName));
            log.debug("DynamoDB table already exists: {}", tableName);
            return;
        } catch (ResourceNotFoundException ignored) {
            // continue → create table
        }

        try {
            DynamoTableSchema schema =
                    DynamoSchemaExtractor.extract(entityClass);

            /* =====================================================
               ✅ BUILD ATTRIBUTE DEFINITIONS (KEYS ONLY)
               ===================================================== */

            Set<String> keyAttributes = new LinkedHashSet<>();

            // Table keys
            keyAttributes.add(schema.partitionKey());
            if (schema.sortKey() != null) {
                keyAttributes.add(schema.sortKey());
            }

            // GSI keys
            for (DynamoGsiDefinition gsi : schema.globalSecondaryIndexes().values()) {
                keyAttributes.add(gsi.getPartitionKey());
                if (gsi.getSortKey() != null) {
                    keyAttributes.add(gsi.getSortKey());
                }
            }

            List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            for (String keyAttr : keyAttributes) {
                attributeDefinitions.add(
                        AttributeDefinition.builder()
                                .attributeName(keyAttr)
                                .attributeType(
                                        ScalarAttributeType.fromValue(
                                                schema.attributes().get(keyAttr)
                                        )
                                )
                                .build()
                );
            }

            /* =====================================================
               ✅ KEY SCHEMA
               ===================================================== */

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
                            .attributeDefinitions(attributeDefinitions)
                            .keySchema(keySchema);

            /* =====================================================
               ✅ GLOBAL SECONDARY INDEXES
               ===================================================== */

            if (!schema.globalSecondaryIndexes().isEmpty()) {
                List<GlobalSecondaryIndex> gsis = new ArrayList<>();
                schema.globalSecondaryIndexes().values()
                        .forEach(gsi ->
                                gsis.add(
                                        gsi.toAwsGsi(
                                                entity.readCapacity(),
                                                entity.writeCapacity()
                                        )
                                )
                        );
                builder.globalSecondaryIndexes(gsis);
            }

            /* =====================================================
               ✅ BILLING MODE
               ===================================================== */

            if (entity.billingMode().isProvisioned()) {
                builder.provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(entity.readCapacity())
                                .writeCapacityUnits(entity.writeCapacity())
                                .build()
                );
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
