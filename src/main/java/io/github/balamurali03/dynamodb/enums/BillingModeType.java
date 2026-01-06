package io.github.balamurali03.dynamodb.enums;

public enum BillingModeType {
    PAY_PER_REQUEST,
    PROVISIONED;

    public boolean isProvisioned() {
        return this == PROVISIONED;
    }
}