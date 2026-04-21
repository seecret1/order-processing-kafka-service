package com.github.seecret1.order_service.client;

import com.github.seecret1.commondto.model.order.CreateOrderRequest;
import com.github.seecret1.commondto.model.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
@RequiredArgsConstructor
public class OrderHttpClient {

    private static final Logger log = LoggerFactory.getLogger(OrderHttpClient.class);

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
        public OrderCreatedEvent saveOrderService(CreateOrderRequest request) {
        log.info("Try save order: {}", request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> req = new HttpEntity<>(request, headers);

        OrderCreatedEvent response = restTemplate.postForObject(dataServiceUrlApi, req, OrderCreatedEvent.class);
        if (response == null) {
            throw new RestClientException("Empty response from data-service");
        }

        log.debug("Successfully save order: {}", request);
        return response;
    }

    @Recover
    public OrderCreatedEvent retrySaveOrder(RestClientException ex, CreateOrderRequest request) {
        log.error("Error push order request: {}. Exception: {}",
                request, ex.getMessage());
        throw new RestClientException("Error push order request", ex);
    }
}
