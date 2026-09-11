package com.cookflow.dto.ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.cookflow.domain.IngredientUnit;

public record UpdateIngredientRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull IngredientUnit defaultUnit
) {
}
