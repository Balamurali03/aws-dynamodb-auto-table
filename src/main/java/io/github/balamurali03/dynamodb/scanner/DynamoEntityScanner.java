package io.github.balamurali03.dynamodb.scanner;

import io.github.balamurali03.dynamodb.annotation.DynamoEntity;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.beans.factory.config.BeanDefinition;

import java.util.HashSet;
import java.util.Set;

public class DynamoEntityScanner {

    private final ApplicationContext context;

    public DynamoEntityScanner(ApplicationContext context) {
        this.context = context;
    }

    public Set<Class<?>> scan() {

        Set<Class<?>> entities = new HashSet<>();

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(
                new AnnotationTypeFilter(DynamoEntity.class)
        );

        for (String basePackage : AutoConfigurationPackages.get(context)) {
            for (BeanDefinition beanDef :
                    scanner.findCandidateComponents(basePackage)) {

                try {
                    entities.add(
                            Class.forName(beanDef.getBeanClassName())
                    );
                } catch (ClassNotFoundException ex) {
                    throw new IllegalStateException(
                            "Failed to load DynamoEntity class: "
                                    + beanDef.getBeanClassName(),
                            ex
                    );
                }
            }
        }

        return entities;
    }
}
