package com.example.logistics.listener;

import com.example.common.constants.JmsConstants;
import com.example.common.dto.OrderDTO;
import com.example.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "jms.implementation", havingValue = "spring", matchIfMissing = true)
public class OrderCreatedListener {
    
    private final LogisticsService logisticsService;
    
    @JmsListener(destination = JmsConstants.ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderDTO orderDTO) {
        log.info("收到新订单: {}", orderDTO);
        try {
            logisticsService.processNewOrder(orderDTO);
            log.info("订单 {} 已添加到物流系统", orderDTO.getId());
        } catch (Exception e) {
            log.error("处理新订单失败", e);
        }
    }
} 