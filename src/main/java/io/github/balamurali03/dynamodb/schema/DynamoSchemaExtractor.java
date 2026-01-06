package io.github.balamurali03.dynamodb.schema;

import io.github.balamurali03.dynamodb.annotation.DynamoEntity;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.*;

public final class DynamoSchemaExtractor {

    private DynamoSchemaExtractor() {
    }

    public static DynamoTableSchema extract(Class<?> entityClass) {

        if (!entityClass.isAnnotationPresent(DynamoDbBean.class)) {
            throw new IllegalStateException(
                    "Entity must be annotated with @DynamoDbBean: " + entityClass.getName());
        }

        DynamoEntity entity =
                entityClass.getAnnotation(DynamoEntity.class);

        Map<String, String> attributes = new LinkedHashMap<>();
        String partitionKey = null;
        String sortKey = null;
        Map<String, DynamoGsiDefinition> gsis = new LinkedHashMap<>();

        for (Field field : entityClass.getDeclaredFields()) {

            String attributeName = field.getName();
            Class<?> type = field.getType();

            if (field.isAnnotationPresent(DynamoDbAttribute.class)) {
                attributeName =
                        field.getAnnotation(DynamoDbAttribute.class).value();
            }

            attributes.put(attributeName, resolveScalarType(type));

            if (field.isAnnotationPresent(DynamoDbPartitionKey.class)) {
                partitionKey = attributeName;
            }

            if (field.isAnnotationPresent(DynamoDbSortKey.class)) {
                sortKey = attributeName;
            }

            if (field.isAnnotationPresent(DynamoDbSecondaryPartitionKey.class)) {
                DynamoDbSecondaryPartitionKey gsi =
                        field.getAnnotation(DynamoDbSecondaryPartitionKey.class);

                for (String index : gsi.indexNames()) {
                    gsis.computeIfAbsent(index, DynamoGsiDefinition::new)
                            .setPartitionKey(attributeName);
                }
            }

            if (field.isAnnotationPresent(DynamoDbSecondarySortKey.class)) {
                DynamoDbSecondarySortKey gsi =
                        field.getAnnotation(DynamoDbSecondarySortKey.class);

                for (String index : gsi.indexNames()) {
                    gsis.computeIfAbsent(index, DynamoGsiDefinition::new)
                            .setSortKey(attributeName);
                }
            }
        }

        if (partitionKey == null) {
            throw new IllegalStateException(
                    "No @DynamoDbPartitionKey defined for " + entityClass.getName());
        }

        return new DynamoTableSchema(
                entity.tableName(),
                partitionKey,
                sortKey,
                attributes,
                gsis
        );
    }

    private static String resolveScalarType(Class<?> type) {

        // Optional<T>
        if (Optional.class.isAssignableFrom(type)) {
            return "S";
        }

        // Enum
        if (Enum.class.isAssignableFrom(type)) {
            return "S";
        }

        // String-like
        if (type == String.class || type == Character.class || type == char.class) {
            return "S";
        }

        // Boolean
        if (type == Boolean.class || type == boolean.class) {
            return "BOOL";
        }

        // Numbers
        if (Number.class.isAssignableFrom(type)
                || (type.isPrimitive() && type != boolean.class && type != char.class)) {
            return "N";
        }

        // Binary
        if (type == byte[].class) {
            return "B";
        }

        // Sets
        if (Set.class.isAssignableFrom(type)) {
            return "SS";
        }

        // Collections / Maps
        if (List.class.isAssignableFrom(type)) {
            return "L";
        }
        if (Map.class.isAssignableFrom(type)) {
            return "M";
        }

        // Date / Time
        if (Temporal.class.isAssignableFrom(type)
                || type.getName().startsWith("java.time")
                || type.getName().equals("java.util.Date")) {
            return "S";
        }

        // Safe default
        return "S";
    }
}
