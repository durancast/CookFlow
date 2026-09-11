package com.cookflow.dto.order;

import com.cookflow.domain.OrderStatus;

import java.time.LocalDateTime;

public record UpdateOrderStatusRequest(
        OrderStatus status
) {
}
