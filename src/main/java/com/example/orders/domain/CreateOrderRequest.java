package com.example.orders.domain;

public record CreateOrderRequest(String customerId, String product, int quantity) { }
