package com.github.seecret1.data.service;

import com.github.seecret1.commondto.model.OrderCreatedEvent;
import com.github.seecret1.data.mapper.OrderMapper;
import com.github.seecret1.data.repository.OrderRepository;
import com.github.seecret1.data.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDataService {

    private final OrderMapper orderMapper;

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    public OrderCreatedEvent getOrderCreatedEvent(UUID orderId) {
        log.info("Get Order Created Event by id: {}", orderId);

        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Order not found by id: " + orderId
                ));

        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderCreatedEvent saveOrder(OrderCreatedEvent orderCreatedEvent) {
        log.info("Save Order Created Event by id: {}", orderCreatedEvent);

        var user = userRepository.findById(orderCreatedEvent.userId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found by id: " + orderCreatedEvent.userId()
                ));

        var order = orderMapper.toEntity(orderCreatedEvent, user);

        orderRepository.save(order);
        return orderCreatedEvent;
    }
}
