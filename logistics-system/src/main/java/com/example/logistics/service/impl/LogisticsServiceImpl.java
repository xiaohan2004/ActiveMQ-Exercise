package com.example.logistics.service.impl;

import com.example.common.constants.JmsConstants;
import com.example.common.dto.LogisticsUpdateDTO;
import com.example.common.dto.OrderDTO;
import com.example.common.model.OrderStatus;
import com.example.logistics.entity.LogisticsOrder;
import com.example.logistics.entity.LogisticsOrderItem;
import com.example.logistics.repository.LogisticsOrderRepository;
import com.example.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsServiceImpl implements LogisticsService {

    private final LogisticsOrderRepository logisticsOrderRepository;
    private final JmsTemplate jmsTemplate;

    @Override
    @Transactional
    public LogisticsOrder processNewOrder(OrderDTO orderDTO) {
        // 创建物流订单
        LogisticsOrder logisticsOrder = new LogisticsOrder();
        logisticsOrder.setId(orderDTO.getId());
        logisticsOrder.setStatus(OrderStatus.PENDING);
        logisticsOrder.setCreatedAt(LocalDateTime.now());
        
        // 添加订单项
        for (OrderDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            LogisticsOrderItem item = new LogisticsOrderItem();
            item.setProductName(itemDTO.getProductName());
            item.setPrice(itemDTO.getPrice());
            item.setQuantity(itemDTO.getQuantity());
            logisticsOrder.addItem(item);
        }
        
        return logisticsOrderRepository.save(logisticsOrder);
    }

    @Override
    @Transactional
    public LogisticsOrder shipOrder(Long orderId) {
        LogisticsOrder order = logisticsOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到物流订单: " + orderId));
        
        order.setStatus(OrderStatus.SHIPPED);
        LogisticsOrder updatedOrder = logisticsOrderRepository.save(order);
        
        // 发送物流状态更新消息到订单系统
        LogisticsUpdateDTO updateDTO = new LogisticsUpdateDTO(
                orderId,
                OrderStatus.SHIPPED,
                LocalDateTime.now()
        );
        
        jmsTemplate.convertAndSend(JmsConstants.LOGISTICS_UPDATED_QUEUE, updateDTO);
        log.info("订单 {} 已发货，消息已发送", orderId);
        
        return updatedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogisticsOrder> getPendingOrders() {
        return logisticsOrderRepository.findByStatus(OrderStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogisticsOrder> getShippedOrders() {
        return logisticsOrderRepository.findByStatus(OrderStatus.SHIPPED);
    }

    @Override
    @Transactional(readOnly = true)
    public LogisticsOrder getOrder(Long orderId) {
        return logisticsOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到物流订单: " + orderId));
    }
} 