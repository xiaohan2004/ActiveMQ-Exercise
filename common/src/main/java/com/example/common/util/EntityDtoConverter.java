package com.example.common.util;

import com.example.common.dto.LogisticsOrderDTO;
import com.example.common.dto.LogisticsOrderDTO.LogisticsOrderItemDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实体与DTO之间的转换工具类
 */
public class EntityDtoConverter {
    
    /**
     * 将物流订单实体转换为DTO，避免循环引用
     * 
     * @param logisticsOrder 物流订单实体对象
     * @return 物流订单DTO
     */
    public static <T> LogisticsOrderDTO convertToDto(T logisticsOrder) {
        try {
            Class<?> clazz = logisticsOrder.getClass();
            
            Long id = (Long) clazz.getMethod("getId").invoke(logisticsOrder);
            Object status = clazz.getMethod("getStatus").invoke(logisticsOrder);
            Object createdAt = clazz.getMethod("getCreatedAt").invoke(logisticsOrder);
            Object updatedAt = clazz.getMethod("getUpdatedAt").invoke(logisticsOrder);
            Object items = clazz.getMethod("getItems").invoke(logisticsOrder);
            Object totalAmount = clazz.getMethod("getTotalAmount").invoke(logisticsOrder);
            
            LogisticsOrderDTO dto = new LogisticsOrderDTO();
            dto.setId(id);
            dto.setStatus((com.example.common.model.OrderStatus) status);
            dto.setCreatedAt((java.time.LocalDateTime) createdAt);
            dto.setUpdatedAt((java.time.LocalDateTime) updatedAt);
            dto.setTotalAmount((java.math.BigDecimal) totalAmount);
            
            // 转换订单项
            if (items instanceof List) {
                List<?> itemsList = (List<?>) items;
                List<LogisticsOrderItemDTO> itemDtos = itemsList.stream()
                        .map(item -> convertItemToDto(item))
                        .collect(Collectors.toList());
                dto.setItems(itemDtos);
            }
            
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("转换实体到DTO失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将物流订单项实体转换为DTO
     * 
     * @param item 物流订单项实体
     * @return 物流订单项DTO
     */
    private static LogisticsOrderItemDTO convertItemToDto(Object item) {
        try {
            Class<?> clazz = item.getClass();
            
            Long id = (Long) clazz.getMethod("getId").invoke(item);
            String productName = (String) clazz.getMethod("getProductName").invoke(item);
            java.math.BigDecimal price = (java.math.BigDecimal) clazz.getMethod("getPrice").invoke(item);
            Integer quantity = (Integer) clazz.getMethod("getQuantity").invoke(item);
            
            return new LogisticsOrderItemDTO(id, productName, price, quantity);
        } catch (Exception e) {
            throw new RuntimeException("转换订单项到DTO失败: " + e.getMessage(), e);
        }
    }
} 