package com.cookflow.dto.table;

import com.cookflow.dto.order.OrderItemDto;

import java.math.BigDecimal;
import java.util.List;

public record ActiveOrderDto(
        Long orderId,
        String status,
        BigDecimal total,
        List<OrderItemDto> items
) {
}
