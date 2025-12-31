package com.framework.dynamodb.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import com.framework.dynamodb.initializer.DynamoTableInitializer;
import com.framework.dynamodb.scanner.DynamoEntityScanner;
import com.framework.dynamodb.table.DynamoTableCreator;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@AutoConfiguration

public class DynamoAutoConfiguration {

    @Bean
    DynamoEntityScanner dynamoEntityScanner(
            ApplicationContext applicationContext) {
        return new DynamoEntityScanner(applicationContext);
    }

    @Bean
    DynamoTableCreator dynamoTableCreator(DynamoDbClient client) {
        return new DynamoTableCreator(client);
    }

    @Bean
    DynamoTableInitializer dynamoTableInitializer(
    		DynamoDbClient client,
            DynamoEntityScanner scanner
            ) {
        return new DynamoTableInitializer(client,scanner);
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> runOnStartup(
            DynamoTableInitializer initializer) {
        return event -> initializer.initialize();
    }
}
