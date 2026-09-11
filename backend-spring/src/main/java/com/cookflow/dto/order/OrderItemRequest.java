package com.cookflow.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderItemRequest(
        @NotNull Long dishId,
        @NotNull @Positive Integer quantity,
        @Size(max = 500) String notes
) {
}
