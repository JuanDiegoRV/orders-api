package com.example.orders.adapter;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.example.orders.application.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrderApiHandlerTest {
    @Test void postReturns201AndLocation() {
        var handler = new OrderApiHandler(new OrderService(), new ObjectMapper());
        var request = new APIGatewayProxyRequestEvent().withHttpMethod("POST")
                .withBody("{\"customerId\":\"CUST-42\",\"product\":\"MacBook Air\",\"quantity\":1}");
        var response = handler.handleRequest(request, null);
        assertEquals(201, response.getStatusCode());
        assertEquals("/orders/ORD-001", response.getHeaders().get("Location"));
    }

    @Test void getMissingReturns404() {
        var handler = new OrderApiHandler(new OrderService(), new ObjectMapper());
        var request = new APIGatewayProxyRequestEvent().withHttpMethod("GET").withPathParameters(Map.of("id", "ORD-999"));
        var response = handler.handleRequest(request, null);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("ORDER_NOT_FOUND"));
    }

    @Test void invalidPostReturns400() {
        var handler = new OrderApiHandler(new OrderService(), new ObjectMapper());
        var request = new APIGatewayProxyRequestEvent().withHttpMethod("POST").withBody("{\"customerId\":\"C\",\"product\":\"A\",\"quantity\":0}");
        assertEquals(400, handler.handleRequest(request, null).getStatusCode());
    }
}
