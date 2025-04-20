package com.example.logistics.controller;

import com.example.common.dto.LogisticsOrderDTO;
import com.example.common.util.EntityDtoConverter;
import com.example.logistics.entity.LogisticsOrder;
import com.example.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/logistics")
public class LogisticsController {
    
    private final LogisticsService logisticsService;
    
    @GetMapping
    public String listPendingOrders(Model model) {
        List<LogisticsOrder> pendingOrders = logisticsService.getPendingOrders();
        // 将实体列表转换为DTO列表
        List<LogisticsOrderDTO> pendingOrderDTOs = pendingOrders.stream()
                .map(EntityDtoConverter::convertToDto)
                .collect(Collectors.toList());
        model.addAttribute("orders", pendingOrderDTOs);
        return "logistics/list";
    }
    
    @GetMapping("/shipped")
    public String listShippedOrders(Model model) {
        List<LogisticsOrder> shippedOrders = logisticsService.getShippedOrders();
        // 将实体列表转换为DTO列表
        List<LogisticsOrderDTO> shippedOrderDTOs = shippedOrders.stream()
                .map(EntityDtoConverter::convertToDto)
                .collect(Collectors.toList());
        model.addAttribute("orders", shippedOrderDTOs);
        return "logistics/shipped";
    }
    
    @PostMapping("/{orderId}/ship")
    @ResponseBody
    public ResponseEntity<LogisticsOrderDTO> shipOrder(@PathVariable Long orderId) {
        LogisticsOrder shippedOrder = logisticsService.shipOrder(orderId);
        // 转换为DTO返回
        LogisticsOrderDTO dto = EntityDtoConverter.convertToDto(shippedOrder);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/{orderId}")
    @ResponseBody
    public ResponseEntity<LogisticsOrderDTO> getOrder(@PathVariable Long orderId) {
        LogisticsOrder order = logisticsService.getOrder(orderId);
        // 转换为DTO返回
        LogisticsOrderDTO dto = EntityDtoConverter.convertToDto(order);
        return ResponseEntity.ok(dto);
    }
}