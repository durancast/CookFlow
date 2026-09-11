package com.cookflow.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long tableId,
        @NotEmpty @Valid List<OrderItemRequest> items
) {
}
