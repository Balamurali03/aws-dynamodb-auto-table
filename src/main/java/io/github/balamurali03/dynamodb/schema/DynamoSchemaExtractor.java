package io.github.balamurali03.dynamodb.schema;

import io.github.balamurali03.dynamodb.annotation.DynamoEntity;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.temporal.Temporal;
import java.util.*;

public final class DynamoSchemaExtractor {

    private DynamoSchemaExtractor() {}

    public static DynamoTableSchema extract(Class<?> entityClass) {

        if (!entityClass.isAnnotationPresent(DynamoDbBean.class)) {
            throw new IllegalStateException(
                    "Entity must be annotated with @DynamoDbBean: " + entityClass.getName());
        }

        DynamoEntity entity = entityClass.getAnnotation(DynamoEntity.class);

        Map<String, String> attributes = new LinkedHashMap<>();
        String partitionKey = null;
        String sortKey = null;
        Map<String, DynamoGsiDefinition> gsis = new LinkedHashMap<>();

        /* ======================================================
           1️⃣ METHOD SCANNING (AWS Enhanced Client style)
           ====================================================== */
        for (Method method : entityClass.getMethods()) {

            String attributeName = resolveAttributeFromGetter(method.getName());
            if (attributeName == null) continue;

            if (method.isAnnotationPresent(DynamoDbPartitionKey.class)) {
                partitionKey = attributeName;
            }

            if (method.isAnnotationPresent(DynamoDbSortKey.class)) {
                sortKey = attributeName;
            }

            if (method.isAnnotationPresent(DynamoDbSecondaryPartitionKey.class)) {
                DynamoDbSecondaryPartitionKey gsi =
                        method.getAnnotation(DynamoDbSecondaryPartitionKey.class);

                for (String index : gsi.indexNames()) {
                    gsis.computeIfAbsent(index, DynamoGsiDefinition::new)
                            .setPartitionKey(attributeName);
                }
            }

            if (method.isAnnotationPresent(DynamoDbSecondarySortKey.class)) {
                DynamoDbSecondarySortKey gsi =
                        method.getAnnotation(DynamoDbSecondarySortKey.class);

                for (String index : gsi.indexNames()) {
                    gsis.computeIfAbsent(index, DynamoGsiDefinition::new)
                            .setSortKey(attributeName);
                }
            }
        }

        /* ======================================================
           2️⃣ FIELD SCANNING (fallback + attributes)
           ====================================================== */
        for (Field field : entityClass.getDeclaredFields()) {

            String attributeName = field.getName();
            Class<?> type = field.getType();

            if (field.isAnnotationPresent(DynamoDbAttribute.class)) {
                attributeName =
                        field.getAnnotation(DynamoDbAttribute.class).value();
            }

            attributes.put(attributeName, resolveScalarType(type));

            if (partitionKey == null &&
                    field.isAnnotationPresent(DynamoDbPartitionKey.class)) {
                partitionKey = attributeName;
            }

            if (sortKey == null &&
                    field.isAnnotationPresent(DynamoDbSortKey.class)) {
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

    /* ======================================================
       Helper: resolve getter → attribute name
       ====================================================== */
    private static String resolveAttributeFromGetter(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            String name = methodName.substring(3);
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return null;
    }

    /* ======================================================
       Type resolution (unchanged, good implementation)
       ====================================================== */
    private static String resolveScalarType(Class<?> type) {

        if (Optional.class.isAssignableFrom(type)) return "S";
        if (Enum.class.isAssignableFrom(type)) return "S";
        if (type == String.class || type == Character.class || type == char.class) return "S";
        if (type == Boolean.class || type == boolean.class) return "BOOL";

        if (Number.class.isAssignableFrom(type)
                || (type.isPrimitive() && type != boolean.class && type != char.class)) {
            return "N";
        }

        if (type == byte[].class) return "B";
        if (Set.class.isAssignableFrom(type)) return "SS";
        if (List.class.isAssignableFrom(type)) return "L";
        if (Map.class.isAssignableFrom(type)) return "M";

        if (Temporal.class.isAssignableFrom(type)
                || type.getName().startsWith("java.time")
                || type.getName().equals("java.util.Date")) {
            return "S";
        }

        return "S";
    }
}
