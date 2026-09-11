package com.cookflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/** PK compuesta (dish_id, ingredient_id). La composición no lleva tenant_id (hereda vía dish). */
@Entity
@Table(name = "dish_ingredients")
@IdClass(DishIngredientId.class)
public class DishIngredient {

    @Id
    @Column(name = "dish_id", nullable = false)
    private Long dishId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id", insertable = false, updatable = false)
    private Dish dish;

    @Id
    @Column(name = "ingredient_id", nullable = false)
    private Long ingredientId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", insertable = false, updatable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private IngredientUnit unit;

    public DishIngredient() {
    }

    public DishIngredient(Long dishId, Long ingredientId, BigDecimal quantity, IngredientUnit unit) {
        this.dishId = dishId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
        if (dish != null && dish.getId() != null) {
            this.dishId = dish.getId();
        }
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        if (ingredient != null && ingredient.getId() != null) {
            this.ingredientId = ingredient.getId();
        }
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public IngredientUnit getUnit() {
        return unit;
    }

    public void setUnit(IngredientUnit unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DishIngredient other)) {
            return false;
        }
        return Objects.equals(dishId, other.dishId) && Objects.equals(ingredientId, other.ingredientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, ingredientId);
    }
}
