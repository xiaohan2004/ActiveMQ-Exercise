package com.example.order.service.impl;

import com.example.common.dto.OrderDTO;
import com.example.common.model.OrderStatus;
import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.example.order.jms.RawJmsOrderSender;
import com.example.order.repository.OrderRepository;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用底层JMS API的订单服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "jms.implementation", havingValue = "raw")
@Primary // 标记为主要实现，覆盖原有实现
public class RawJmsOrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RawJmsOrderSender rawJmsOrderSender;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.info("创建新订单(通过原始JMS实现)");
        
        // 创建订单实体
        Order order = new Order();
        order.setItems(new ArrayList<>());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        // 添加订单项
        for (OrderDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductName(itemDTO.getProductName());
            item.setPrice(itemDTO.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            order.addItem(item);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // 转换为DTO
        OrderDTO savedOrderDTO = convertToDto(savedOrder);
        
        // 发送消息到物流系统
        rawJmsOrderSender.sendNewOrder(savedOrderDTO);
        log.info("订单 {} 已创建，通过原始JMS API发送到物流系统", savedOrder.getId());
        
        return savedOrderDTO;
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("更新订单状态(通过原始JMS实现): 订单ID={}, 新状态={}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到订单: " + orderId));
        
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        return convertToDto(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到订单: " + orderId));
        return convertToDto(order);
    }

    /**
     * 将订单实体转换为DTO
     */
    private OrderDTO convertToDto(Order order) {
        List<OrderDTO.OrderItemDTO> itemDtos = order.getItems().stream()
                .map(item -> new OrderDTO.OrderItemDTO(
                        item.getId(),
                        item.getProductName(),
                        item.getPrice(),
                        item.getQuantity()))
                .collect(Collectors.toList());
        
        return new OrderDTO(
                order.getId(),
                itemDtos,
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
} 