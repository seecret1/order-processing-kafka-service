package com.github.seecret1.data.controller;

import com.github.seecret1.commondto.model.order.CreateOrderRequest;
import com.github.seecret1.commondto.model.order.OrderCreatedEvent;
import com.github.seecret1.data.service.OrderDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class OrderDataController {

    private final OrderDataService orderDataService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderCreatedEvent> getOrderCreatedEvent(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(orderDataService.getOrderCreatedEvent(id));
    }

    @PostMapping
    public ResponseEntity<OrderCreatedEvent> saveOrder(
            @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.ok(orderDataService.saveOrder(request));
    }
}
