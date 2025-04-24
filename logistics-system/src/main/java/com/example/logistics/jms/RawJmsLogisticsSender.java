package com.example.logistics.jms;

import com.example.common.constants.JmsConstants;
import com.example.common.dto.LogisticsUpdateDTO;
import com.example.common.jms.RawJmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 使用底层JMS API的物流系统消息发送服务
 * 用于向订单系统发送物流状态更新
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "jms.implementation", havingValue = "raw")
public class RawJmsLogisticsSender {

    private final RawJmsService rawJmsService;
    
    /**
     * 发送物流状态更新消息到订单系统
     * 
     * @param updateDTO 更新信息DTO
     */
    public void sendLogisticsUpdate(LogisticsUpdateDTO updateDTO) {
        try {
            rawJmsService.sendObjectMessage(JmsConstants.LOGISTICS_UPDATED_QUEUE, updateDTO);
            log.info("已发送物流状态更新消息(通过原始JMS API): 订单ID={}, 新状态={}", 
                    updateDTO.getOrderId(), updateDTO.getNewStatus());
        } catch (Exception e) {
            log.error("发送物流状态更新消息失败", e);
            throw new RuntimeException("发送物流状态更新消息失败", e);
        }
    }
} 