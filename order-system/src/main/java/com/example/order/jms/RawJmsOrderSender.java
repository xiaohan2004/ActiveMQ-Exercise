package com.example.order.jms;

import com.example.common.constants.JmsConstants;
import com.example.common.dto.OrderDTO;
import com.example.common.jms.RawJmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 使用底层JMS API的订单系统消息发送服务
 * 用于向物流系统发送新订单通知
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "jms.implementation", havingValue = "raw")
public class RawJmsOrderSender {

    private final RawJmsService rawJmsService;
    
    /**
     * 发送新订单消息到物流系统
     * 
     * @param orderDTO 订单DTO
     */
    public void sendNewOrder(OrderDTO orderDTO) {
        try {
            rawJmsService.sendObjectMessage(JmsConstants.ORDER_CREATED_QUEUE, orderDTO);
            log.info("已发送新订单消息(通过原始JMS API): 订单ID={}", orderDTO.getId());
        } catch (Exception e) {
            log.error("发送新订单消息失败", e);
            throw new RuntimeException("发送新订单消息失败", e);
        }
    }
} 