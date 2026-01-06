package io.github.balamurali03.dynamodb.config;

import io.github.balamurali03.dynamodb.core.DynamoTableInitializer;
import io.github.balamurali03.dynamodb.scanner.DynamoEntityScanner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@AutoConfiguration
@ConditionalOnClass(DynamoDbClient.class)
public class DynamoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DynamoEntityScanner dynamoEntityScanner(
            ApplicationContext applicationContext) {
        return new DynamoEntityScanner(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamoTableInitializer dynamoTableInitializer(
            DynamoDbClient client) {
        return new DynamoTableInitializer(client);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> dynamoStartupListener(
            DynamoEntityScanner scanner,
            DynamoTableInitializer initializer) {

        return event -> {
            scanner.scan().forEach(initializer::initialize);
        };
    }
}
