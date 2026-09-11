package com.cookflow.dto.dish;

import java.math.BigDecimal;
import java.util.List;

/** Vista detalle de plato (GET /api/dishes/{id}). */
public record DishDetailDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Boolean available,
        String imageUrl,
        DishSummaryDto.CategoryRef category,
        List<DishIngredientDto> ingredients
) {
}
