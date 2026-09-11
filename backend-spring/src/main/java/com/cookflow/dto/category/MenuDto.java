package com.cookflow.dto.category;

import com.cookflow.dto.dish.DishSummaryDto;

import java.util.List;

/** Catálogo público (GET /api/public/menu). */
public record MenuDto(
        List<CategoryWithDishes> categories
) {
    public record CategoryWithDishes(Long id, String name, String slug, List<DishSummaryDto> dishes) {
    }
}
