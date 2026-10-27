package com.example.orders.application;

import com.example.orders.domain.CreateOrderRequest;
import com.example.orders.domain.InvalidOrderException;
import com.example.orders.domain.OrderNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {
    @Test void createsValidOrder() {
        var service = new OrderService();
        var order = service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));
        assertEquals("ORD-001", order.id());
        assertEquals("CUST-42", order.customerId());
    }

    @Test void retrievesExistingOrder() {
        var service = new OrderService();
        var created = service.createOrder(new CreateOrderRequest("CUST-42", "Keyboard", 2));
        assertEquals(created, service.getOrder(created.id()));
    }

    @Test void missingOrderThrows() {
        assertThrows(OrderNotFoundException.class, () -> new OrderService().getOrder("ORD-999"));
    }

    @Test void filtersByCustomer() {
        var service = new OrderService();
        service.createOrder(new CreateOrderRequest("CUST-42", "A", 1));
        service.createOrder(new CreateOrderRequest("CUST-100", "B", 1));
        assertEquals(1, service.getOrdersByCustomer("CUST-42").size());
    }

    @Test void rejectsInvalidInput() {
        var service = new OrderService();
        assertThrows(InvalidOrderException.class, () -> service.createOrder(new CreateOrderRequest("", "A", 1)));
        assertThrows(InvalidOrderException.class, () -> service.createOrder(new CreateOrderRequest("C", "A", 0)));
    }

    @Test void deletesExistingOrderAndRejectsSecondDelete() {
        var service = new OrderService();
        var order = service.createOrder(new CreateOrderRequest("C", "A", 1));
        service.deleteOrder(order.id());
        assertThrows(OrderNotFoundException.class, () -> service.deleteOrder(order.id()));
    }
}
