package com.example.order.jms;

import com.example.common.constants.JmsConstants;
import com.example.common.dto.LogisticsUpdateDTO;
import com.example.common.jms.RawJmsService;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 使用底层JMS API的订单系统消息监听器
 * 用于从物流系统接收状态更新消息
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "jms.implementation", havingValue = "raw")
public class RawJmsOrderListener {

    private final RawJmsService rawJmsService;
    private final OrderService orderService;
    
    @PostConstruct
    public void init() {
        // 注册监听器处理来自物流系统的状态更新消息
        rawJmsService.registerListener(JmsConstants.LOGISTICS_UPDATED_QUEUE, this::handleLogisticsUpdate);
        
        log.info("初始化原始JMS监听器，监听队列: {}", JmsConstants.LOGISTICS_UPDATED_QUEUE);
    }
    
    /**
     * 处理物流状态更新消息
     */
    private void handleLogisticsUpdate(LogisticsUpdateDTO updateDTO) {
        log.info("收到物流状态更新消息(通过原始JMS API): 订单ID={}, 新状态={}", 
                updateDTO.getOrderId(), updateDTO.getNewStatus());
        try {
            orderService.updateOrderStatus(
                    updateDTO.getOrderId(),
                    updateDTO.getNewStatus()
            );
            log.info("订单 {} 状态已更新为 {}", updateDTO.getOrderId(), updateDTO.getNewStatus());
        } catch (Exception e) {
            log.error("处理物流状态更新消息失败", e);
        }
    }
} 