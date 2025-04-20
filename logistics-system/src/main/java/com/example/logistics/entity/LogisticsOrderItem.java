package com.example.logistics.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "logistics_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logistics_order_id")
    @JsonBackReference
    private LogisticsOrder logisticsOrder;
    
    private String productName;
    
    private BigDecimal price;
    
    private Integer quantity;
} 