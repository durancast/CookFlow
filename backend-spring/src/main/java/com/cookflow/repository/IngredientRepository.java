package com.cookflow.repository;

import com.cookflow.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByTenantId(Long tenantId);

    Optional<Ingredient> findByTenantIdAndId(Long tenantId, Long id);

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);
}
