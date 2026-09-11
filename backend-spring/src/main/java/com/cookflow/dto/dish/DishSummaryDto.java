package com.cookflow.dto.dish;

/** Vista resumen (lista de platos). */
public record DishSummaryDto(
        Long id,
        String name,
        java.math.BigDecimal price,
        Boolean available,
        String imageUrl,
        CategoryRef category
) {
    public record CategoryRef(Long id, String name) {
    }
}
