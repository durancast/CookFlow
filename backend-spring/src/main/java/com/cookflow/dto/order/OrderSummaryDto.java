package com.cookflow.dto.order;

import com.cookflow.domain.OrderStatus;

import java.time.LocalDateTime;

public record OrderSummaryDto(
        Long id,
        Integer tableNumber,
        OrderStatus status,
        java.math.BigDecimal total,
        LocalDateTime createdAt
) {
}
