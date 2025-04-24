package com.example.order.listener;

import com.example.common.constants.JmsConstants;
import com.example.common.dto.LogisticsUpdateDTO;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "jms.implementation", havingValue = "spring", matchIfMissing = true)
public class LogisticsUpdateListener {
    
    private final OrderService orderService;
    
    @JmsListener(destination = JmsConstants.LOGISTICS_UPDATED_QUEUE)
    public void onLogisticsUpdate(LogisticsUpdateDTO updateDTO) {
        log.info("收到物流状态更新: {}", updateDTO);
        try {
            orderService.updateOrderStatus(updateDTO.getOrderId(), updateDTO.getNewStatus());
            log.info("订单 {} 状态已更新为 {}", updateDTO.getOrderId(), updateDTO.getNewStatus());
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
        }
    }
} 