package com.example.common.model;

public enum OrderStatus {
    PENDING("未发货"), 
    SHIPPED("已发货");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 