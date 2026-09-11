package com.cookflow.service;

import com.cookflow.domain.Ingredient;
import com.cookflow.domain.IngredientUnit;
import com.cookflow.dto.ingredient.CreateIngredientRequest;
import com.cookflow.dto.ingredient.IngredientDto;
import com.cookflow.dto.ingredient.UpdateIngredientRequest;
import com.cookflow.exception.ConflictException;
import com.cookflow.exception.NotFoundException;
import com.cookflow.repository.DishIngredientRepository;
import com.cookflow.repository.IngredientRepository;
import com.cookflow.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final DishIngredientRepository dishIngredientRepository;

    public IngredientService(IngredientRepository ingredientRepository,
                             DishIngredientRepository dishIngredientRepository) {
        this.ingredientRepository = ingredientRepository;
        this.dishIngredientRepository = dishIngredientRepository;
    }

    public List<IngredientDto> list() {
        long tenantId = TenantContext.requireTenantId();
        return ingredientRepository.findAllByTenantId(tenantId).stream()
                .map(i -> new IngredientDto(i.getId(), i.getName(), i.getDefaultUnit()))
                .toList();
    }

    public IngredientDto get(Long id) {
        long tenantId = TenantContext.requireTenantId();
        Ingredient i = requireIngredient(tenantId, id);
        return new IngredientDto(i.getId(), i.getName(), i.getDefaultUnit());
    }

    @Transactional
    public IngredientDto create(CreateIngredientRequest req) {
        long tenantId = TenantContext.requireTenantId();
        if (ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, req.name())) {
            throw new ConflictException("Ya existe un ingrediente llamado '" + req.name() + "'");
        }
        Ingredient i = new Ingredient(tenantId, req.name(), req.defaultUnit());
        return new IngredientDto(i.getId(), i.getName(), i.getDefaultUnit());
    }

    @Transactional
    public IngredientDto update(Long id, UpdateIngredientRequest req) {
        long tenantId = TenantContext.requireTenantId();
        Ingredient i = requireIngredient(tenantId, id);
        if (!i.getName().equalsIgnoreCase(req.name())
                && ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, req.name())) {
            throw new ConflictException("Ya existe un ingrediente llamado '" + req.name() + "'");
        }
        i.setName(req.name());
        i.setDefaultUnit(req.defaultUnit());
        return new IngredientDto(i.getId(), i.getName(), i.getDefaultUnit());
    }

    @Transactional
    public void delete(Long id) {
        long tenantId = TenantContext.requireTenantId();
        Ingredient i = requireIngredient(tenantId, id);
        if (dishIngredientRepository.existsByIngredientId(id)) {
            throw new ConflictException("No se puede eliminar el ingrediente: hay platos que lo usan");
        }
        ingredientRepository.delete(i);
    }

    private Ingredient requireIngredient(long tenantId, Long id) {
        return ingredientRepository.findById(id)
                .filter(i -> Objects.equals(i.getTenantId(), tenantId))
                .orElseThrow(() -> NotFoundException.resource("Ingrediente", id));
    }
}
