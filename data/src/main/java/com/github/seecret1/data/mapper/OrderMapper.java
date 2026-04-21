package com.github.seecret1.data.mapper;

import com.github.seecret1.commondto.model.CreateOrderRequest;
import com.github.seecret1.commondto.model.OrderCreatedEvent;
import com.github.seecret1.data.entity.Order;
import com.github.seecret1.data.entity.User;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toEntity(
            CreateOrderRequest dto,
            User user
    ) {
        return Order.builder()
                .user(user)
                .productCode(dto.productCode())
                .quantity(dto.quantity())
                .price(dto.price())
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
