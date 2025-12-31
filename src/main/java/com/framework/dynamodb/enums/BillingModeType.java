package com.framework.dynamodb.enums;

public enum BillingModeType {
    PAY_PER_REQUEST,
    PROVISIONED;

    public boolean isProvisioned() {
        return this == PROVISIONED;
    }
}