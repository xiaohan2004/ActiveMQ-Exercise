package com.example.order.service;

import com.example.common.dto.OrderDTO;
import com.example.common.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(OrderDTO orderDTO);
    OrderDTO updateOrderStatus(Long orderId, OrderStatus status);
    OrderDTO getOrder(Long orderId);
    List<OrderDTO> getAllOrders();
} 