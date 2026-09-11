package com.cookflow.domain;

import java.io.Serializable;
import java.util.Objects;

/** @IdClass para la PK compuesta de {@link DishIngredient}. */
public class DishIngredientId implements Serializable {

    private Long dishId;
    private Long ingredientId;

    public DishIngredientId() {
    }

    public DishIngredientId(Long dishId, Long ingredientId) {
        this.dishId = dishId;
        this.ingredientId = ingredientId;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DishIngredientId other)) {
            return false;
        }
        return Objects.equals(dishId, other.dishId) && Objects.equals(ingredientId, other.ingredientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, ingredientId);
    }
}
