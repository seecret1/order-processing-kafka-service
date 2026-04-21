package com.github.seecret1.notification_service.listener;

import com.github.seecret1.commondto.model.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrderListener {

    @RetryableTopic(attempts = "1", kafkaTemplate = "retryableTemplate")
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
        log.info("Received order: {}", order);
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                key, partition, topic, timestamp);
    }

    @DltHandler
    public void dltListener(
            OrderCreatedEvent order,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Get message with error: {}; Topic: {}; Offset: {}",
                order, topic, offset);
    }
}
