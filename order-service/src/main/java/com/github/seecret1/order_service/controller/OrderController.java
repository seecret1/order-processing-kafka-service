package com.github.seecret1.order_service.controller;

import com.github.seecret1.commondto.model.CreateOrderRequest;
import com.github.seecret1.commondto.model.OrderCreatedEvent;
import jakarta.validation.Valid;
import com.github.seecret1.order_service.service.OrderKafkaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderKafkaService orderKafkaService;

    @PostMapping
    public ResponseEntity<OrderCreatedEvent> sendOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.ok(orderKafkaService.saveOrder(request));
    }
}
