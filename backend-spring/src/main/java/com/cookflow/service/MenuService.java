package com.cookflow.service;

import com.cookflow.domain.Category;
import com.cookflow.domain.Dish;
import com.cookflow.dto.category.MenuDto;
import com.cookflow.dto.dish.DishSummaryDto;
import com.cookflow.repository.CategoryRepository;
import com.cookflow.repository.DishRepository;
import com.cookflow.repository.TenantRepository;
import com.cookflow.security.TenantContext;
import com.cookflow.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Catálogo público multi-tenant (GET /api/public/menu). */
@Service
@Transactional(readOnly = true)
public class MenuService {

    private static final long DEFAULT_TENANT_ID = 1L;

    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final TenantRepository tenantRepository;

    public MenuService(CategoryRepository categoryRepository,
                       DishRepository dishRepository,
                       TenantRepository tenantRepository) {
        this.categoryRepository = categoryRepository;
        this.dishRepository = dishRepository;
        this.tenantRepository = tenantRepository;
    }

    /**
     * El tenant se resuelve en orden: slug explícito en query param
     * (&lt;code&gt;?tenant=acme&lt;/code&gt;), usuario autenticado, y por defecto tenant_id=1.
     */
    public MenuDto publicMenu(String tenantSlug) {
        long tenantId = resolveTenantId(tenantSlug);
        List<Category> categories = categoryRepository.findAllByTenantId(tenantId);
        List<MenuDto.CategoryWithDishes> result = categories.stream()
                .map(c -> {
                    List<DishSummaryDto> dishes = dishRepository
                            .findAllByTenantIdAndCategoryIdAndAvailableTrue(tenantId, c.getId())
                            .stream()
                            .map(d -> toSummary(d, c))
                            .toList();
                    return new MenuDto.CategoryWithDishes(c.getId(), c.getName(), c.getSlug(), dishes);
                })
                .toList();
        return new MenuDto(result);
    }

    private long resolveTenantId(String slug) {
        if (slug != null && !slug.isBlank()) {
            return tenantRepository.findBySlug(slug.trim())
                    .map(t -> t.getId())
                    .orElse(-1L);
        }
        return TenantContext.currentTenantId().orElse(DEFAULT_TENANT_ID);
    }

    private DishSummaryDto toSummary(Dish d, Category c) {
        return new DishSummaryDto(
                d.getId(),
                d.getName(),
                d.getPrice(),
                d.getAvailable(),
                d.getImageUrl(),
                new DishSummaryDto.CategoryRef(c.getId(), c.getName())
        );
    }
}
