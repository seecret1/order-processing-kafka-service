package com.github.seecret1.order_service.client;

import com.github.seecret1.commondto.model.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderHttpClient {

    private final RestTemplate restTemplate;

    @Value("${app.data.service.url.api}")
    private String dataServiceUrlApi;

    @Retryable(
            value = {RestClientException.class},
            maxAttemptsExpression = "${app.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${app.retry.delay:1000}",
                    multiplierExpression = "${app.retry.multiplier:2}"
            )
    )
    public void saveOrderService(OrderCreatedEvent order) {
        log.info("Try save order: {}", order.orderId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderCreatedEvent> request = new HttpEntity<>(order, headers);

        restTemplate.postForObject(dataServiceUrlApi, request, OrderCreatedEvent.class);

        log.info("HTTP request completed for order: {}", order.orderId());
        log.debug("Successfully save order: {}", order);
    }

    @Recover
    public void retrySaveOrder(RestClientException ex, OrderCreatedEvent order) {
        log.error("Error push order request: {}. Exception: {}",
                order.orderId(), ex.getMessage());
        throw new RestClientException("Error push order request: " + order.orderId());
    }
}
