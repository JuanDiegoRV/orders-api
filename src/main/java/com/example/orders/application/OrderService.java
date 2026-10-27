package com.example.orders.application;

import com.example.orders.domain.CreateOrderRequest;
import com.example.orders.domain.InvalidOrderException;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Plain Java application logic. It has no HTTP, Lambda, or API Gateway dependency. */
public class OrderService {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    public Order createOrder(CreateOrderRequest request) {
        validate(request);
        String id = "ORD-%03d".formatted(sequence.incrementAndGet());
        Order order = new Order(id, request.customerId().trim(), request.product().trim(), request.quantity());
        orders.put(id, order);
        return order;
    }

    public Order getOrder(String id) {
        requireText(id, "id");
        Order order = orders.get(id);
        if (order == null) throw new OrderNotFoundException(id);
        return order;
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        requireText(customerId, "customerId");
        return orders.values().stream()
                .filter(order -> order.customerId().equals(customerId.trim()))
                .sorted(Comparator.comparing(Order::id))
                .toList();
    }

    public void deleteOrder(String id) {
        requireText(id, "id");
        if (orders.remove(id) == null) throw new OrderNotFoundException(id);
    }

    private static void validate(CreateOrderRequest request) {
        if (request == null) throw new InvalidOrderException("Request body is required");
        requireText(request.customerId(), "customerId");
        requireText(request.product(), "product");
        if (request.quantity() <= 0) throw new InvalidOrderException("quantity must be greater than zero");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new InvalidOrderException(field + " is required");
    }
}
