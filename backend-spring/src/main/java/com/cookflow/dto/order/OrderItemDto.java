package com.cookflow.dto.order;

import java.math.BigDecimal;

public record OrderItemDto(
        Long dishId,
        String dishName,
        Integer quantity,
        BigDecimal unitPrice,
        String notes
) {
}
