package com.cloudorder.order.service;

import com.cloudorder.order.repository.OutboxItem;
import com.cloudorder.order.repository.OutboxRepository;
import com.cloudorder.order.repository.OutboxStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OutboxPublisher {
    private static final int MAX_RETRIES = 5;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void publish() {

        for (OutboxItem item :
                outboxRepository.findPending()
                        .items()) {

            publish(item);
        }
    }

    private void publish(OutboxItem item) {

        kafkaTemplate
                .send(
                        "order.created",
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
    public void markPublished(
            OutboxItem item) {

        item.setStatus(
                OutboxStatus.PUBLISHED.name()
        );

        item.setPublishedAt(
                Instant.now().toString()
        );

        item.setGsi1pk(null);
        item.setGsi1sk(null);

        outboxRepository.table().updateItem(item);
    }
    private void handleSuccess(
            OutboxItem item) {

        outboxRepository.markPublished(item);
    }

    private void handleFailure(
            OutboxItem item,
            Throwable exception) {

        int retryCount =
                item.getRetryCount() + 1;

        item.setRetryCount(retryCount);

        if (retryCount >= MAX_RETRIES) {

            outboxRepository.markFailed(item);

        } else {

            outboxRepository.updateRetry(item);
        }
    }
}
