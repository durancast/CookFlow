package com.cookflow.service;

import com.cookflow.domain.Category;
import com.cookflow.domain.Dish;
import com.cookflow.domain.DishIngredient;
import com.cookflow.domain.Ingredient;
import com.cookflow.dto.dish.CreateDishRequest;
import com.cookflow.dto.dish.DishDetailDto;
import com.cookflow.dto.dish.DishIngredientDto;
import com.cookflow.dto.dish.DishSummaryDto;
import com.cookflow.dto.dish.UpdateDishIngredientRequest;
import com.cookflow.dto.dish.UpdateDishRequest;
import com.cookflow.exception.ConflictException;
import com.cookflow.exception.NotFoundException;
import com.cookflow.repository.CategoryRepository;
import com.cookflow.repository.DishIngredientRepository;
import com.cookflow.repository.DishRepository;
import com.cookflow.repository.IngredientRepository;
import com.cookflow.repository.OrderItemRepository;
import com.cookflow.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DishService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final DishIngredientRepository dishIngredientRepository;
    private final OrderItemRepository orderItemRepository;

    public DishService(DishRepository dishRepository,
                       CategoryRepository categoryRepository,
                       IngredientRepository ingredientRepository,
                       DishIngredientRepository dishIngredientRepository,
                       OrderItemRepository orderItemRepository) {
        this.dishRepository = dishRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.dishIngredientRepository = dishIngredientRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public List<DishSummaryDto> list(Boolean available, Long categoryId) {
        long tenantId = TenantContext.requireTenantId();
        List<Dish> dishes;
        if (categoryId != null && Boolean.TRUE.equals(available)) {
            dishes = dishRepository.findAllByTenantIdAndCategoryIdAndAvailableTrue(tenantId, categoryId);
        }
        else if (categoryId != null) {
            dishes = dishRepository.findAllByTenantIdAndCategoryId(tenantId, categoryId);
        }
        else if (Boolean.TRUE.equals(available)) {
            dishes = dishRepository.findAllByTenantIdAndAvailableTrue(tenantId);
        }
        else {
            dishes = dishRepository.findAllByTenantId(tenantId);
        }
        Map<Long, Category> categories = loadCategories(tenantId);
        return dishes.stream().map(d -> toSummary(d, categories)).toList();
    }

    public DishDetailDto get(Long id) {
        long tenantId = TenantContext.requireTenantId();
        Dish dish = requireDish(tenantId, id);
        Map<Long, Category> categories = loadCategories(tenantId);
        List<DishIngredientDto> ingredients = ingredientsOf(tenantId, dish.getId());
        return toDetail(dish, categories, ingredients);
    }

    @Transactional
    public DishSummaryDto create(CreateDishRequest req) {
        long tenantId = TenantContext.requireTenantId();
        requireCategory(tenantId, req.categoryId());
        Dish dish = new Dish(tenantId, req.categoryId(), req.name(),
                req.price() == null ? BigDecimal.ZERO : req.price());
        dish.setDescription(req.description());
        dish.setAvailable(req.available() == null ? Boolean.TRUE : req.available());
        dish = dishRepository.save(dish);
        Map<Long, Category> categories = loadCategories(tenantId);
        return toSummary(dish, categories);
    }

    @Transactional
    public DishSummaryDto update(Long id, UpdateDishRequest req) {
        long tenantId = TenantContext.requireTenantId();
        Dish dish = requireDish(tenantId, id);
        requireCategory(tenantId, req.categoryId());
        dish.setName(req.name());
        dish.setDescription(req.description());
        dish.setCategoryId(req.categoryId());
        dish.setPrice(req.price());
        dish.setAvailable(req.available() == null ? Boolean.TRUE : req.available());
        dish = dishRepository.save(dish);
        Map<Long, Category> categories = loadCategories(tenantId);
        return toSummary(dish, categories);
    }

    @Transactional
    public void delete(Long id) {
        long tenantId = TenantContext.requireTenantId();
        Dish dish = requireDish(tenantId, id);
        if (orderItemRepository.existsByDishId(id)) {
            throw new ConflictException("No se puede eliminar el plato: hay pedidos que lo usan");
        }
        dishIngredientRepository.deleteAllByDishId(id);
        dishRepository.delete(dish);
    }

    public List<DishIngredientDto> ingredients(Long dishId) {
        long tenantId = TenantContext.requireTenantId();
        requireDish(tenantId, dishId);
        return ingredientsOf(tenantId, dishId);
    }

    @Transactional
    public List<DishIngredientDto> replaceIngredients(Long dishId, List<UpdateDishIngredientRequest> items) {
        long tenantId = TenantContext.requireTenantId();
        requireDish(tenantId, dishId);
        Map<Long, Ingredient> byId = ingredientRepository.findAllByTenantId(tenantId).stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        dishIngredientRepository.deleteAllByDishId(dishId);
        List<DishIngredient> saved = items.stream()
                .map(ri -> {
                    Ingredient ing = byId.get(ri.ingredientId());
                    if (ing == null) {
                        throw new NotFoundException("Ingrediente " + ri.ingredientId() + " no encontrado del tenant");
                    }
                    return dishIngredientRepository.save(
                            new DishIngredient(dishId, ri.ingredientId(), ri.quantity(), ri.unit()));
                })
                .toList();
        return saved.stream()
                .map(di -> new DishIngredientDto(di.getIngredientId(),
                        byId.get(di.getIngredientId()).getName(), di.getQuantity(), di.getUnit()))
                .toList();
    }

    private List<DishIngredientDto> ingredientsOf(long tenantId, Long dishId) {
        List<DishIngredient> list = dishIngredientRepository.findAllByDishId(dishId);
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = list.stream().map(DishIngredient::getIngredientId).collect(Collectors.toSet());
        Map<Long, Ingredient> byId = ingredientRepository.findAllById(ids).stream()
                .filter(ing -> Objects.equals(ing.getTenantId(), tenantId))
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        return list.stream()
                .map(di -> {
                    Ingredient ing = byId.get(di.getIngredientId());
                    return new DishIngredientDto(di.getIngredientId(),
                            ing == null ? null : ing.getName(), di.getQuantity(), di.getUnit());
                })
                .toList();
    }

    private Map<Long, Category> loadCategories(long tenantId) {
        return categoryRepository.findAllByTenantId(tenantId).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    private void requireCategory(long tenantId, Long categoryId) {
        boolean ok = categoryRepository.findById(categoryId)
                .map(c -> Objects.equals(c.getTenantId(), tenantId))
                .orElse(false);
        if (!ok) {
            throw new NotFoundException("Categoría " + categoryId + " no encontrada");
        }
    }

    private Dish requireDish(long tenantId, Long id) {
        return dishRepository.findById(id)
                .filter(d -> Objects.equals(d.getTenantId(), tenantId))
                .orElseThrow(() -> NotFoundException.resource("Plato", id));
    }

    private static DishSummaryDto toSummary(Dish d, Map<Long, Category> categories) {
        Category c = categories.get(d.getCategoryId());
        DishSummaryDto.CategoryRef ref = c == null
                ? new DishSummaryDto.CategoryRef(d.getCategoryId(), null)
                : new DishSummaryDto.CategoryRef(c.getId(), c.getName());
        return new DishSummaryDto(d.getId(), d.getName(), d.getPrice(), d.getAvailable(), d.getImageUrl(), ref);
    }

    private static DishDetailDto toDetail(Dish d, Map<Long, Category> categories, List<DishIngredientDto> ingredients) {
        Category c = categories.get(d.getCategoryId());
        DishSummaryDto.CategoryRef ref = c == null
                ? new DishSummaryDto.CategoryRef(d.getCategoryId(), null)
                : new DishSummaryDto.CategoryRef(c.getId(), c.getName());
        return new DishDetailDto(d.getId(), d.getName(), d.getDescription(), d.getPrice(),
                d.getAvailable(), d.getImageUrl(), ref, ingredients);
    }
}
