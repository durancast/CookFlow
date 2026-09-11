package com.cookflow.dto.dish;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.cookflow.domain.IngredientUnit;

import java.math.BigDecimal;

public record UpdateDishIngredientRequest(
        @NotNull Long ingredientId,
        @NotNull @DecimalMin("0.001") BigDecimal quantity,
        @NotNull IngredientUnit unit
) {
}
