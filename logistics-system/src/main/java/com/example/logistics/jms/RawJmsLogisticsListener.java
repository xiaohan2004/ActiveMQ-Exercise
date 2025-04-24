package com.example.logistics.jms;

import com.example.common.constants.JmsConstants;
import com.example.common.dto.OrderDTO;
import com.example.common.jms.RawJmsService;
import com.example.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 使用底层JMS API的物流系统消息监听器
 * 用于从订单系统接收新订单消息
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "jms.implementation", havingValue = "raw")
public class RawJmsLogisticsListener {

    private final RawJmsService rawJmsService;
    private final LogisticsService logisticsService;
    
    @PostConstruct
    public void init() {
        // 注册监听器处理来自订单系统的消息
        rawJmsService.registerListener(JmsConstants.ORDER_CREATED_QUEUE, this::handleOrderCreated);
        
        log.info("初始化原始JMS监听器，监听队列: {}", JmsConstants.ORDER_CREATED_QUEUE);
    }
    
    /**
     * 处理新订单创建消息
     */
    private void handleOrderCreated(OrderDTO orderDTO) {
        log.info("收到新订单消息(通过原始JMS API): {}", orderDTO);
        try {
            logisticsService.processNewOrder(orderDTO);
            log.info("订单 {} 已添加到物流系统", orderDTO.getId());
        } catch (Exception e) {
            log.error("处理新订单失败", e);
        }
    }
} 