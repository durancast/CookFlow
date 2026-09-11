package com.cookflow;

import com.cookflow.domain.Category;
import com.cookflow.domain.DiningTable;
import com.cookflow.domain.Dish;
import com.cookflow.domain.Ingredient;
import com.cookflow.domain.IngredientUnit;
import com.cookflow.domain.User;
import com.cookflow.domain.UserRole;
import com.cookflow.repository.CategoryRepository;
import com.cookflow.repository.DiningTableRepository;
import com.cookflow.repository.DishRepository;
import com.cookflow.repository.IngredientRepository;
import com.cookflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Random;

public final class TestSeed {

    public static final String ADMIN_EMAIL = "admin@cookflow.test";
    public static final String WAITER_EMAIL = "waiter@cookflow.test";
    public static final String KITCHEN_EMAIL = "kitchen@cookflow.test";
    public static final String MANAGER_EMAIL = "manager@cookflow.test";
    public static final String PASSWORD = "password123";

    private TestSeed() {
    }

    /**
     * Crea (idempotentemente) usuarios de prueba en el tenant seed (id=1).
     * Llamar antes de cada test para garantizar el estado mínimo.
     */
    public static void seedUsers(UserRepository userRepository, PasswordEncoder encoder) {
        long tenantId = 1L;
        ensureUser(userRepository, tenantId, "Admin", ADMIN_EMAIL, UserRole.admin, encoder);
        ensureUser(userRepository, tenantId, "Waiter", WAITER_EMAIL, UserRole.waiter, encoder);
        ensureUser(userRepository, tenantId, "Kitchen", KITCHEN_EMAIL, UserRole.kitchen, encoder);
        ensureUser(userRepository, tenantId, "Manager", MANAGER_EMAIL, UserRole.manager, encoder);
    }

    /** Crea una mesa fresca. Devuelve su id. */
    public static Long newTable(DiningTableRepository repo) {
        Random rnd = new Random();
        int number = 1000 + rnd.nextInt(89999);
        while (repo.existsByTenantIdAndNumber(1L, number)) {
            number = 1000 + rnd.nextInt(89999);
        }
        return repo.save(new DiningTable(1L, number, 4)).getId();
    }

    /** Crea una categoría y un plato disponible en ella. Devuelve el id del plato. */
    public static Long newDish(CategoryRepository categoryRepo, DishRepository dishRepo) {
        Random rnd = new Random();
        String suffix = String.valueOf(rnd.nextInt(1_000_000));
        Category cat = categoryRepo.save(new Category(1L, "Cat-" + suffix, "cat-" + suffix));
        Dish dish = new Dish(1L, cat.getId(), "Plato-" + suffix, new BigDecimal("10.00"));
        dish.setAvailable(Boolean.TRUE);
        return dishRepo.save(dish).getId();
    }

    /** Crea un ingrediente fresco con nombre único. Devuelve su id. */
    public static Long newIngredient(IngredientRepository repo) {
        String name = "Ing-" + new Random().nextInt(1_000_000);
        return repo.save(new Ingredient(1L, name, IngredientUnit.G)).getId();
    }

    public static void setDishAvailable(DishRepository dishRepo, Long dishId, boolean available) {
        dishRepo.findById(dishId).ifPresent(d -> {
            d.setAvailable(available);
            dishRepo.save(d);
        });
    }

    private static void ensureUser(UserRepository repo, long tenantId, String name,
                                   String email, UserRole role, PasswordEncoder encoder) {
        if (repo.findByEmail(email).isEmpty()) {
            repo.save(new User(tenantId, name, email, encoder.encode(PASSWORD), role));
        }
    }
}
