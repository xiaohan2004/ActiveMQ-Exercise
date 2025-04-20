package com.example.logistics.repository;

import com.example.common.model.OrderStatus;
import com.example.logistics.entity.LogisticsOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogisticsOrderRepository extends JpaRepository<LogisticsOrder, Long> {
    List<LogisticsOrder> findByStatus(OrderStatus status);
}