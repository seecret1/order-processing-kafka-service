package com.github.seecret1.order_service.service;

import com.github.seecret1.commondto.model.CreateOrderRequest;
import com.github.seecret1.commondto.model.OrderCreatedEvent;
import com.github.seecret1.order_service.client.OrderHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaService {

    @Value("${app.kafka.topic}")
    private String topicName;

    private final OrderHttpClient orderHttpClient;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderCreatedEvent saveOrder(CreateOrderRequest request) {
        try {
            log.info("Saving order in data-service");
            OrderCreatedEvent order = orderHttpClient.saveOrderService(request);

            log.info("Sending order to Kafka");
            kafkaRetryTemplate.execute(context -> {
                try {
                    kafkaTemplate.send(
                            topicName,
                            order.orderId().toString(),
                            order
                    ).get();

                    log.info("Order sent to kafka successfully: {}", order.orderId());
                    return null;
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Failed to send message to Kafka", e);
                    throw new RuntimeException("Kafka send failed", e);
                }
            });
            log.debug("Order {} saved in db and sent in kafka", order);
            return order;

        } catch (RestClientException ex) {
            log.error("Failed to save order for request: {}", request, ex);
            throw new RuntimeException("Failed to save order", ex);
        }
    }
}
