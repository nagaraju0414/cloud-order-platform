package com.cloudorder.order.service;

import com.cloudorder.order.repository.OutboxItem;
import com.cloudorder.order.repository.OutboxRepository;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;

@Component
public class OutboxPublisher {

    private static final String
            ORDER_CREATED_TOPIC =
            "order.created";

    private static final int
            MAX_RETRIES = 5;

    private final OutboxRepository
            outboxRepository;

    private final KafkaTemplate<String, String>
            kafkaTemplate;

    public OutboxPublisher(

            OutboxRepository outboxRepository,

            KafkaTemplate<String, String>
                    kafkaTemplate) {

        this.outboxRepository =
                outboxRepository;

        this.kafkaTemplate =
                kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void publish() {

        for (OutboxItem item :
                outboxRepository.findPending()) {

            publishItem(item);
        }
    }

    private void publishItem(
            OutboxItem item) {

        kafkaTemplate
                .send(

                        ORDER_CREATED_TOPIC,

                        item.getAggregateId(),

                        item.getPayload()

                )

                .whenComplete(
                        (result, exception) -> {

                            if (exception != null) {

                                handleFailure(
                                        item,
                                        exception
                                );

                            } else {

                                handleSuccess(
                                        item
                                );
                            }
                        }
                );
    }

    private void handleSuccess(
            OutboxItem item) {

        outboxRepository
                .markPublished(item);
    }

    private void handleFailure(

            OutboxItem item,

            Throwable exception) {

        int retryCount =
                item.getRetryCount() + 1;

        item.setRetryCount(
                retryCount
        );

        if (retryCount >= MAX_RETRIES) {

            outboxRepository
                    .markFailed(item);

        } else {

            outboxRepository
                    .updateRetry(item);
        }
    }
}