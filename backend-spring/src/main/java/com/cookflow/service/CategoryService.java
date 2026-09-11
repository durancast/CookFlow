package com.cookflow.service;

import com.cookflow.domain.Category;
import com.cookflow.dto.category.CategoryDto;
import com.cookflow.dto.category.CreateCategoryRequest;
import com.cookflow.dto.category.MenuDto;
import com.cookflow.dto.category.UpdateCategoryRequest;
import com.cookflow.dto.dish.DishSummaryDto;
import com.cookflow.exception.ConflictException;
import com.cookflow.exception.NotFoundException;
import com.cookflow.repository.CategoryRepository;
import com.cookflow.repository.DishRepository;
import com.cookflow.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;

    public CategoryService(CategoryRepository categoryRepository, DishRepository dishRepository) {
        this.categoryRepository = categoryRepository;
        this.dishRepository = dishRepository;
    }

    public List<CategoryDto> list() {
        long tenantId = TenantContext.requireTenantId();
        return categoryRepository.findAllByTenantId(tenantId).stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getSlug()))
                .toList();
    }

    public CategoryDto byId(Long id) {
        long tenantId = TenantContext.requireTenantId();
        Category c = requireCategory(tenantId, id);
        return new CategoryDto(c.getId(), c.getName(), c.getSlug());
    }

    /** Catálogo público: las categorías con sus platos disponibles. */
    public MenuDto publicMenu(Long explicitTenantId) {
        long tenantId = explicitTenantId != null ? explicitTenantId : TenantContext.requireTenantId();
        List<com.cookflow.domain.Category> categories = categoryRepository.findAllByTenantId(tenantId);
        Map<Long, List<DishSummaryDto>> dishesByCat = dishRepository
                .findAllByTenantIdAndAvailableTrue(tenantId)
                .stream()
                .collect(Collectors.groupingBy(
                        d -> d.getCategoryId(),
                        Collectors.mapping(d -> toSummary(tenantId, d), Collectors.toList())));
        List<MenuDto.CategoryWithDishes> cats = categories.stream()
                .map(c -> new MenuDto.CategoryWithDishes(c.getId(), c.getName(), c.getSlug(),
                        dishesByCat.getOrDefault(c.getId(), List.of())))
                .toList();
        return new MenuDto(cats);
    }

    @Transactional
    public CategoryDto create(CreateCategoryRequest req) {
        long tenantId = TenantContext.requireTenantId();
        if (categoryRepository.existsByTenantIdAndSlug(tenantId, req.slug())) {
            throw new ConflictException("Ya existe una categoría con el slug '" + req.slug() + "'");
        }
        Category c = new Category(tenantId, req.name(), req.slug());
        return new CategoryDto(c.getId(), c.getName(), c.getSlug());
    }

    @Transactional
    public CategoryDto update(Long id, UpdateCategoryRequest req) {
        long tenantId = TenantContext.requireTenantId();
        Category c = requireCategory(tenantId, id);
        if (!c.getSlug().equals(req.slug())
                && categoryRepository.existsByTenantIdAndSlugAndIdNot(tenantId, req.slug(), id)) {
            throw new ConflictException("Ya existe una categoría con el slug '" + req.slug() + "'");
        }
        c.setName(req.name());
        c.setSlug(req.slug());
        return new CategoryDto(c.getId(), c.getName(), c.getSlug());
    }

    @Transactional
    public void delete(Long id) {
        long tenantId = TenantContext.requireTenantId();
        Category c = requireCategory(tenantId, id);
        if (dishRepository.existsByTenantIdAndCategoryId(tenantId, id)) {
            throw new ConflictException("No se puede eliminar la categoría: hay platos que la usan");
        }
        categoryRepository.delete(c);
    }

    private Category requireCategory(long tenantId, Long id) {
        return categoryRepository.findById(id)
                .filter(c -> Objects.equals(c.getTenantId(), tenantId))
                .orElseThrow(() -> NotFoundException.resource("Categoría", id));
    }

    private DishSummaryDto toSummary(long tenantId, com.cookflow.domain.Dish d) {
        com.cookflow.domain.Category cat = categoryRepository.findById(d.getCategoryId()).orElse(null);
        DishSummaryDto.CategoryRef ref = cat == null
                ? new DishSummaryDto.CategoryRef(d.getCategoryId(), null)
                : new DishSummaryDto.CategoryRef(cat.getId(), cat.getName());
        return new DishSummaryDto(d.getId(), d.getName(), d.getPrice(), d.getAvailable(), d.getImageUrl(), ref);
    }
}
