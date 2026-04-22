package com.github.seecret1.notification_service.listener;

import com.github.seecret1.commondto.model.order.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrderListener {

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.groupId}",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    public void listen(
            @Payload OrderCreatedEvent order,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) UUID key,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(value = KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp
    ) {
        log.info("Order received: orderId={}, userId={}, productCode={}, quantity={}",
                order.orderId(), order.userId(), order.productCode(), order.quantity());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                key, partition, topic, timestamp);

        log.debug("Order: {}", order);

        if (order.quantity() > 100) {
            log.error("Order quantity exceeded");
            throw new RuntimeException("Order quantity limited");
        }
        log.info("Processing order successfully: {}", order.orderId());
    }

    @DltHandler
    public void dltListener(
            OrderCreatedEvent order,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exception
    ) {
        log.error("Message moved to DLT - Order: {}; Original Topic: {}; Offset: {}; Exception: {}",
                order, topic, offset, exception);

        log.debug("Handle order: {}", order);
    }
}
