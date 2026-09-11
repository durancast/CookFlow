package com.cookflow.dto.ingredient;

import com.cookflow.domain.IngredientUnit;

public record IngredientDto(
        Long id,
        String name,
        IngredientUnit defaultUnit
) {
}
