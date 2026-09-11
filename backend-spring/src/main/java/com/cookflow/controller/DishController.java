package com.cookflow.controller;

import com.cookflow.domain.UserRole;
import com.cookflow.dto.dish.DishDetailDto;
import com.cookflow.dto.dish.DishIngredientDto;
import com.cookflow.dto.dish.DishSummaryDto;
import com.cookflow.dto.dish.CreateDishRequest;
import com.cookflow.dto.dish.UpdateDishIngredientRequest;
import com.cookflow.dto.dish.UpdateDishRequest;
import com.cookflow.service.DishService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_KITCHEN')")
    public List<DishSummaryDto> list(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Long categoryId) {
        return dishService.list(available, categoryId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_KITCHEN')")
    public DishDetailDto get(@PathVariable Long id) {
        return dishService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER')")
    public ResponseEntity<DishSummaryDto> create(@Valid @RequestBody CreateDishRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dishService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER')")
    public DishSummaryDto update(@PathVariable Long id, @Valid @RequestBody UpdateDishRequest request) {
        return dishService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dishService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ingredients")
    public List<DishIngredientDto> ingredients(@PathVariable Long id) {
        return dishService.ingredients(id);
    }

    @PutMapping("/{id}/ingredients")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER')")
    public List<DishIngredientDto> replaceIngredients(@PathVariable Long id,
                                                       @Valid @NotEmpty @RequestBody List<UpdateDishIngredientRequest> items) {
        return dishService.replaceIngredients(id, items);
    }
}
