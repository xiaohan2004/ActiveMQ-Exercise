package com.example.common.dto;

import com.example.common.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流订单DTO - 用于API响应，避免实体类的循环引用问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsOrderDTO {
    private Long id;
    private List<LogisticsOrderItemDTO> items = new ArrayList<>();
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal totalAmount;
    
    /**
     * 物流订单项DTO - 不包含对物流订单的引用
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogisticsOrderItemDTO {
        private Long id;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
    }
} 