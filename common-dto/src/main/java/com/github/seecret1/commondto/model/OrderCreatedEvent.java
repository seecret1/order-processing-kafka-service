package com.github.seecret1.commondto.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(

        UUID orderId,

        @NotBlank(message = "user id must be set!")
        String userId,

        @NotBlank(message = "product code must be set!")
        String productCode,

        @Positive(message = "quantity must be positive value!")
        int quantity,

        @NotNull(message = "price must be set!")
        @Positive(message = "price must be positive value!")
        BigDecimal price,

        Instant timestamp
) {
        public OrderCreatedEvent(
                String userId,
                String productCode,
                int quantity,
                BigDecimal price
        ) {
                this(
                        UUID.randomUUID(),
                        userId,
                        productCode,
                        quantity,
                        price,
                        Instant.now()
                );
        }
}
