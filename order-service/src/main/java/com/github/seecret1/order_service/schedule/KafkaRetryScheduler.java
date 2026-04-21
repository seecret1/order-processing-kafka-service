package com.github.seecret1.order_service.schedule;

import com.github.seecret1.commondto.model.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(KafkaRetryScheduler.class);

    @Value("${app.kafka.topic}")
    private String topicName;

    private static final long FIXED_DELAY = 5000L;

    private static final int BATCH_SIZE = 10;

    private final FailedOrderQueue queue;

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Scheduled(fixedDelay = FIXED_DELAY)
    public void retry() {
        int processed = 0;
        OrderCreatedEvent order;

        while ((order = queue.poll()) != null && processed < BATCH_SIZE) {
            processed++;
            try {
                kafkaTemplate.send(
                        topicName,
                        order.orderId().toString(),
                        order
                ).get();
                log.info("Retry success: {}", order.orderId());

            } catch (Exception ex) {
                log.error("Retry failed again: {}", order.orderId(), ex);
                queue.add(order);
            }
        }
    }
}
