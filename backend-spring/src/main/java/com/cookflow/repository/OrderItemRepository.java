package com.cookflow.repository;

import com.cookflow.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderId(Long orderId);

    List<OrderItem> findAllByOrderIdIn(List<Long> orderIds);

    long countByOrderId(Long orderId);

    boolean existsByDishId(Long dishId);
}
