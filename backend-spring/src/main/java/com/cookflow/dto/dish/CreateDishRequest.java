package com.cookflow.dto.dish;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateDishRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotNull Long categoryId,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        Boolean available
) {
    public CreateDishRequest(String name, String description, Long categoryId, BigDecimal price) {
        this(name, description, categoryId, price, Boolean.TRUE);
    }
}
