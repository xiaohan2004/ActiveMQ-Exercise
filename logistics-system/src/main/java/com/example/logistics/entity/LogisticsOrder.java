package com.example.logistics.entity;

import com.example.common.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "logistics_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsOrder {
    @Id
    private Long id; // 与订单系统中的订单ID相同
    
    @OneToMany(mappedBy = "logisticsOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LogisticsOrderItem> items = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public void addItem(LogisticsOrderItem item) {
        items.add(item);
        item.setLogisticsOrder(this);
    }
    
    public void removeItem(LogisticsOrderItem item) {
        items.remove(item);
        item.setLogisticsOrder(null);
    }
    
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 