package com.cloudorder.order.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter ordersFailed;
    private final Timer orderProcessingTime;

    public OrderMetrics(MeterRegistry meterRegistry) {

        this.ordersCreated = Counter.builder("orders.created")
                .description("Number of orders successfully created")
                .register(meterRegistry);

        this.ordersFailed = Counter.builder("orders.failed")
                .description("Number of failed order creations")
                .register(meterRegistry);

        this.orderProcessingTime = Timer.builder("orders.processing.time")
                .description("Time taken to process an order")
                .register(meterRegistry);
    }

    public void incrementOrdersCreated() {
        ordersCreated.increment();
    }

    public void incrementOrdersFailed() {
        ordersFailed.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void recordProcessingTime(Timer.Sample sample) {
        sample.stop(orderProcessingTime);
    }
}
