package com.cloudorder.order.repository;

public enum OutboxStatus {

    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}