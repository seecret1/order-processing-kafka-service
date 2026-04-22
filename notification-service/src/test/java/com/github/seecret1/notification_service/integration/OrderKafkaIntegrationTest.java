package com.github.seecret1.notification_service.integration;

import com.github.seecret1.commondto.model.order.OrderCreatedEvent;
import com.github.seecret1.notification_service.listener.OrderListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class OrderKafkaIntegrationTest {

    @Container
    static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> retryableTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoSpyBean
    private OrderListener orderListener;

    @Value("${app.kafka.topic}")
    private String topicName;

    @Value("${app.kafka.partitions}")
    private int topicPartitions;

    @BeforeEach
    void waitForKafkaConsumersAssignment() {
        applicationContext.getBeanProvider(KafkaListenerEndpointRegistry.class).ifAvailable(
                registry -> registry.getListenerContainers().forEach(
                        container -> ContainerTestUtils.waitForAssignment(container, topicPartitions)
                )
        );
    }

    @Test
    void sendEventInKafka() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                "user-42",
                "SKU-100",
                2,
                new BigDecimal("149.90"),
                Instant.now()
        );

        retryableTemplate.send(topicName, event)
                .get(10, TimeUnit.SECONDS);

        verify(orderListener, timeout(15_000)).listen(
                argThat(received ->
                        received.orderId().equals(event.orderId())
                                && received.userId().equals(event.userId())
                                && received.productCode().equals(event.productCode())
                                && received.quantity() == event.quantity()
                ),
                isNull(),
                eq(topicName),
                anyInt(),
                anyLong()
        );
    }
}
