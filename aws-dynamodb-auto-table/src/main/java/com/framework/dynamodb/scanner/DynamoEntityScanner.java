package com.framework.dynamodb.scanner;

import org.springframework.context.ApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import com.framework.dynamodb.annotation.DynamoEntity;

import java.util.HashSet;
import java.util.Set;

public class DynamoEntityScanner {

    private final ApplicationContext context;

    public DynamoEntityScanner(ApplicationContext context) {
        this.context = context;
    }

    public Set<Class<?>> scan() {

        Set<Class<?>> result = new HashSet<>();

        for (String basePackage :
                org.springframework.boot.autoconfigure.AutoConfigurationPackages.get(context)) {

            var scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(DynamoEntity.class));

            scanner.findCandidateComponents(basePackage)
                    .forEach(bd -> {
                        try {
                            result.add(Class.forName(bd.getBeanClassName()));
                        } catch (ClassNotFoundException ignored) {}
                    });
        }
        return result;
    }
}
