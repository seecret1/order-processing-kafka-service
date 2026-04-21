package com.github.seecret1.order_service.service;

import com.github.seecret1.commondto.model.CreateOrderRequest;
import com.github.seecret1.commondto.model.OrderCreatedEvent;
import com.github.seecret1.order_service.client.OrderHttpClient;
import com.github.seecret1.order_service.schedule.FailedOrderQueue;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class OrderKafkaService {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaService.class);

    @Value("${app.kafka.topic}")
    private String topicName;

    private final OrderHttpClient orderHttpClient;

    private final FailedOrderQueue failedOrderQueue;

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderCreatedEvent saveOrder(CreateOrderRequest request) {
        try {
            log.info("Saving order in data-service");
            OrderCreatedEvent order = orderHttpClient.saveOrderService(request);

            log.info("Sending order to Kafka");
            kafkaTemplate.send(
                    topicName,
                    order.orderId().toString(),
                    order
            ).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Kafka failed, enqueue retry for order: {}", order.orderId());
                    failedOrderQueue.add(order);
                } else {
                    log.info("Order sent to Kafka: {}", order.orderId());
                }
            });

            log.debug("Send order: {}, in Kafka", order);
            log.debug("Order saved in db: {}", order);
            return order;

        } catch (RestClientException ex) {
            log.error("Failed to save order for request: {}", request, ex);
            throw new RuntimeException("Failed to save order", ex);
        }
    }
}
