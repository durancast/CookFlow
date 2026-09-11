package com.cookflow.dto.order;

import com.cookflow.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailDto(
        Long id,
        Integer tableNumber,
        OrderStatus status,
        BigDecimal total,
        LocalDateTime createdAt,
        List<OrderItemDto> items
) {
}
