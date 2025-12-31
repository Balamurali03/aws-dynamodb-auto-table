package com.framework.dynamodb.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.framework.dynamodb.annotation.DynamoEntity;
import com.framework.dynamodb.schema.*;
import com.framework.dynamodb.scanner.DynamoEntityScanner;

import software.amazon.awssdk.services.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

public class DynamoTableInitializer {

    private static final Logger log = LoggerFactory.getLogger(DynamoTableInitializer.class);

    private final DynamoDbClient client;
    private final DynamoEntityScanner scanner;

    public DynamoTableInitializer(
            DynamoDbClient client,
            DynamoEntityScanner scanner) {

        this.client = client;
        this.scanner = scanner;
    }

    public void initialize() {
        scanner.scan().forEach(this::createIfMissing);
    }

    private void createIfMissing(Class<?> entityClass) {

        DynamoEntity entity = entityClass.getAnnotation(DynamoEntity.class);
        if (!entity.autoCreate()) return;

        String tableName = entity.tableName().isBlank()
                ? entityClass.getSimpleName()
                : entity.tableName();

        try {
            client.describeTable(r -> r.tableName(tableName));
            return;
        } catch (ResourceNotFoundException ignored) {}

        DynamoTableSchema schema =
                DynamoSchemaExtractor.extract(entityClass);

        List<AttributeDefinition> attributes = new ArrayList<>();
        schema.attributes().forEach((k, v) ->
                attributes.add(AttributeDefinition.builder()
                        .attributeName(k)
                        .attributeType(ScalarAttributeType.fromValue(v))
                        .build()));

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

        List<GlobalSecondaryIndex> gsis = new ArrayList<>();
        schema.globalSecondaryIndexes().values()
                .forEach(g ->
                        gsis.add(g.toAwsGsi(
                                entity.readCapacity(),
                                entity.writeCapacity()))
                );

        CreateTableRequest.Builder builder =
                CreateTableRequest.builder()
                        .tableName(tableName)
                        .attributeDefinitions(attributes)
                        .keySchema(keySchema);

        if (!gsis.isEmpty()) {
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
        log.info("✅ DynamoDB table created: {}", tableName);
    }
}
