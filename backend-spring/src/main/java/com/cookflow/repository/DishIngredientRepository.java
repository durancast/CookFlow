package com.cookflow.repository;

import com.cookflow.domain.DishIngredient;
import com.cookflow.domain.DishIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DishIngredientRepository extends JpaRepository<DishIngredient, DishIngredientId> {

    List<DishIngredient> findAllByDishId(Long dishId);

    Optional<DishIngredient> findByDishIdAndIngredientId(Long dishId, Long ingredientId);

    void deleteAllByDishId(Long dishId);

    boolean existsByIngredientId(Long ingredientId);
}
