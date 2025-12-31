package com.framework.dynamodb.schema;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.lang.reflect.Method;
import java.util.*;

public final class DynamoSchemaExtractor {

    private DynamoSchemaExtractor() {}

    public static DynamoTableSchema extract(Class<?> entityClass) {

        if (!entityClass.isAnnotationPresent(DynamoDbBean.class)) {
            throw new IllegalStateException(
                    entityClass.getName() + " must be annotated with @DynamoDbBean");
        }

        String tableName = entityClass.getSimpleName();

        String partitionKey = null;
        String sortKey = null;

        Map<String, String> attributes = new HashMap<>();
        Map<String, DynamoGsiDefinition> gsiMap = new HashMap<>();

        for (Method method : entityClass.getMethods()) {

            if (!method.getName().startsWith("get")) continue;

            String attrName = resolveAttributeName(method);
            attributes.putIfAbsent(attrName, resolveScalarType(method.getReturnType()));

            if (method.isAnnotationPresent(DynamoDbPartitionKey.class)) {
                partitionKey = attrName;
            }

            if (method.isAnnotationPresent(DynamoDbSortKey.class)) {
                sortKey = attrName;
            }

            DynamoDbSecondaryPartitionKey gsiPk =
                    method.getAnnotation(DynamoDbSecondaryPartitionKey.class);

            DynamoDbSecondarySortKey gsiSk =
                    method.getAnnotation(DynamoDbSecondarySortKey.class);

            if (gsiPk != null) {
                for (String index : gsiPk.indexNames()) {
                    gsiMap.putIfAbsent(index,
                            new DynamoGsiDefinition(index, attrName, null, List.of()));
                }
            }

            if (gsiSk != null) {
                for (String index : gsiSk.indexNames()) {
                    DynamoGsiDefinition existing = gsiMap.get(index);
                    if (existing == null) {
                        throw new IllegalStateException(
                                "GSI sort key without partition key for index " + index);
                    }
                    gsiMap.put(index,
                            new DynamoGsiDefinition(
                                    index,
                                    existing.partitionKey(),
                                    attrName,
                                    existing.projectedAttributes()));
                }
            }
        }

        if (partitionKey == null) {
            throw new IllegalStateException(
                    "No @DynamoDbPartitionKey found in " + entityClass.getName());
        }

        return new DynamoTableSchema(
                tableName,
                partitionKey,
                sortKey,
                attributes,
                gsiMap
        );
    }

    private static String resolveAttributeName(Method method) {
        DynamoDbAttribute attr = method.getAnnotation(DynamoDbAttribute.class);
        if (attr != null && !attr.value().isBlank()) return attr.value();
        return decapitalize(method.getName().substring(3));
    }

    private static String resolveScalarType(Class<?> type) {
        if (type == String.class) return "S";
        if (type == Boolean.class || type == boolean.class) return "BOOL";
        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) return "N";
        if (type == byte[].class) return "B";
        if (List.class.isAssignableFrom(type)) return "L";
        if (Map.class.isAssignableFrom(type)) return "M";
        return "S";
    }

    private static String decapitalize(String value) {
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }
}
