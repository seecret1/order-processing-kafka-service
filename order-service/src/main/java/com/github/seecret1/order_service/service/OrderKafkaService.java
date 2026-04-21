package com.github.seecret1.order_service.service;

import com.github.seecret1.commondto.model.OrderCreatedEvent;
import com.github.seecret1.order_service.client.OrderHttpClient;
import com.github.seecret1.order_service.schedule.FailedOrderQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaService {

    @Value("${app.kafka.topic}")
    private String topicName;

    private final OrderHttpClient orderHttpClient;

    private final FailedOrderQueue failedOrderQueue;

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void saveOrder(OrderCreatedEvent order) {
        try {
            log.info("Saving order in Kafka");
            orderHttpClient.saveOrderService(order);

            log.info("Sending order to data-service");
            kafkaTemplate.send(
                    topicName,
                    order.orderId().toString(),
                    order
            ).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Kafka failed, enqueue retry: {}", order.orderId());
                    failedOrderQueue.add(order);
                } else {
                    log.info("Order sent to Kafka: {}", order.orderId());
                }
            });

            log.debug("Send order: {}, in Kafka", order);
            log.debug("Order saved in db: {}", order);

        } catch (RestClientException ex) {
            log.error("Failed to save order: {}", order.orderId(), ex);
            throw new RuntimeException("Failed to save order", ex);
        }
    }
}
