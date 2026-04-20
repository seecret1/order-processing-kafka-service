package com.github.seecret1.order_service.schedule;

import com.github.seecret1.commondto.model.OrderCreatedEvent;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class FailedOrderQueue {
    private final Queue<OrderCreatedEvent> queue = new ConcurrentLinkedQueue<>();

    public void add(OrderCreatedEvent order) {
        queue.add(order);
    }

    public OrderCreatedEvent poll() {
        return queue.poll();
    }
}