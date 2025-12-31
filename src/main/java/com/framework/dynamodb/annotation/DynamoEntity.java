package com.framework.dynamodb.annotation;

import java.lang.annotation.*;

import com.framework.dynamodb.enums.BillingModeType;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamoEntity {

    /**
     * Optional table name.
     * Defaults to entity simple class name.
     */
    String tableName() default "";

    /**
     * Auto create table at startup.
     */
    boolean autoCreate() default true;

    /**
     * Billing mode
     */
    BillingModeType billingMode() default BillingModeType.PAY_PER_REQUEST;

    long readCapacity() default 5L;
    long writeCapacity() default 5L;
}


