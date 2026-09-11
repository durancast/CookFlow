package com.cookflow.repository;

import com.cookflow.domain.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findAllByTenantId(Long tenantId);

    List<Dish> findAllByTenantIdAndAvailableTrue(Long tenantId);

    List<Dish> findAllByTenantIdAndCategoryId(Long tenantId, Long categoryId);

    List<Dish> findAllByTenantIdAndCategoryIdAndAvailableTrue(Long tenantId, Long categoryId);

    Optional<Dish> findByTenantIdAndId(Long tenantId, Long id);

    Page<Dish> findAllByTenantId(Long tenantId, Pageable pageable);

    boolean existsByTenantIdAndCategoryId(Long tenantId, Long categoryId);
}
