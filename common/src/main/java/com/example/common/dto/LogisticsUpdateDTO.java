package com.example.common.dto;

import com.example.common.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private OrderStatus newStatus;
    private LocalDateTime updatedAt;
} 