package com.example.orders.domain;

public record Order(String id, String customerId, String product, int quantity) { }
