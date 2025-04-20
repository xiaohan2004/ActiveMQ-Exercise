package com.example.logistics.service;

import com.example.common.dto.OrderDTO;
import com.example.logistics.entity.LogisticsOrder;

import java.util.List;

public interface LogisticsService {
    LogisticsOrder processNewOrder(OrderDTO orderDTO);
    LogisticsOrder shipOrder(Long orderId);
    List<LogisticsOrder> getPendingOrders();
    List<LogisticsOrder> getShippedOrders();
    LogisticsOrder getOrder(Long orderId);
} 