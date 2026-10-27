package com.example.orders.adapter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.orders.application.OrderService;
import com.example.orders.domain.CreateOrderRequest;
import com.example.orders.domain.InvalidOrderException;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** AWS-specific HTTP adapter. Business rules stay in OrderService. */
public class OrderApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final ObjectMapper mapper;
    private final OrderService service;

    public OrderApiHandler() { this(new OrderService(), new ObjectMapper()); }

    OrderApiHandler(OrderService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String method = request == null || request.getHttpMethod() == null ? "" : request.getHttpMethod().toUpperCase();
            Map<String, String> path = request == null || request.getPathParameters() == null
                    ? Map.of() : request.getPathParameters();
            String id = path.get("id");
            String customerId = request == null || request.getQueryStringParameters() == null
                    ? null : request.getQueryStringParameters().get("customerId");

            return switch (method) {
                case "POST" -> created(service.createOrder(mapper.readValue(request.getBody(), CreateOrderRequest.class)));
                case "GET" -> id != null ? ok(service.getOrder(id)) : ok(service.getOrdersByCustomer(customerId));
                case "DELETE" -> { service.deleteOrder(id); yield noContent(); }
                default -> error(405, "METHOD_NOT_ALLOWED", "Unsupported HTTP method");
            };
        } catch (InvalidOrderException | IllegalArgumentException e) {
            return error(400, "INVALID_REQUEST", e.getMessage());
        } catch (OrderNotFoundException e) {
            String id = e.getMessage().replaceFirst("^Order ", "").replaceFirst(" does not exist$", "");
            return error(404, "ORDER_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            if (context != null) context.getLogger().log("Unhandled request error: " + e);
            return error(500, "INTERNAL_ERROR", "An unexpected error occurred");
        }
    }

    private APIGatewayProxyResponseEvent ok(Object body) { return response(200, body); }
    private APIGatewayProxyResponseEvent created(Order order) {
        APIGatewayProxyResponseEvent response = response(201, order);
        response.setHeaders(Map.of("Location", "/orders/" + order.id()));
        return response;
    }
    private APIGatewayProxyResponseEvent noContent() { return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(headers()); }
    private APIGatewayProxyResponseEvent response(int status, Object body) {
        try { return new APIGatewayProxyResponseEvent().withStatusCode(status).withHeaders(headers()).withBody(mapper.writeValueAsString(body)); }
        catch (Exception e) { return error(500, "INTERNAL_ERROR", "Could not serialize response"); }
    }
    private APIGatewayProxyResponseEvent error(int status, String code, String message) {
        return response(status, new ErrorBody(code, message));
    }
    private Map<String, String> headers() { return new LinkedHashMap<>(Map.of("Content-Type", "application/json")); }
    private record ErrorBody(String error, String message) { }
}
