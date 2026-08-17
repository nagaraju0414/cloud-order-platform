package com.cloudorder.order.repository;

import com.cloudorder.order.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(String orderId);

    List<Order> findByCustomerId(String customerId);
}