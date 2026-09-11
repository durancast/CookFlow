package com.cookflow.dto.dish;

import com.cookflow.domain.IngredientUnit;

import java.math.BigDecimal;

public record DishIngredientDto(
        Long ingredientId,
        String ingredientName,
        BigDecimal quantity,
        IngredientUnit unit
) {
}
