package com.cloudorder.order.domain;

public enum OrderStatus {

    CREATED,
    PENDING_INVENTORY,
    INVENTORY_RESERVED,
    PENDING_PAYMENT,
    PAYMENT_AUTHORIZED,
    CONFIRMED,
    CANCELLED,
    SHIPPED,
    DELIVERED,
    FAILED
}