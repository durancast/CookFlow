package com.cookflow.dto.table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTableRequest(
        @NotNull @Positive Integer number,
        @NotNull @Positive Integer capacity
) {
}
