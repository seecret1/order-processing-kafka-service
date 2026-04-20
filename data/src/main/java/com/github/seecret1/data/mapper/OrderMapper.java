package com.github.seecret1.data.mapper;

import com.github.seecret1.commondto.model.OrderCreatedEvent;
import com.github.seecret1.data.entity.Order;
import com.github.seecret1.data.entity.User;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toEntity(
            OrderCreatedEvent dto,
            User user
    ) {
        return Order.builder()
                .id(dto.orderId())
                .user(user)
                .productCode(dto.productCode())
                .quantity(dto.quantity())
                .price(dto.price())
                .timestamp(dto.timestamp())
                .build();
    }

    public OrderCreatedEvent toDto(Order entity) {
        return new OrderCreatedEvent(
                entity.getId(),
                entity.getUser().getId(),
                entity.getProductCode(),
                entity.getQuantity(),
                entity.getPrice(),
                entity.getTimestamp()
        );
    }
}
