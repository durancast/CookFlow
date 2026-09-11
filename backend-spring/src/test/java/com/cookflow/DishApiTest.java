package com.cookflow;

import com.cookflow.domain.Category;
import com.cookflow.domain.Dish;
import com.cookflow.repository.CategoryRepository;
import com.cookflow.repository.DishRepository;
import com.cookflow.repository.IngredientRepository;
import com.cookflow.repository.UserRepository;
import com.cookflow.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = CookFlowApplication.class)
@ActiveProfiles("test")
class DishApiTest extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper om;
    @Autowired CategoryRepository categories;
    @Autowired DishRepository dishes;
    @Autowired IngredientRepository ingredients;

    @BeforeEach
    void seed() {
        TestSeed.seedUsers(users, encoder);
    }

    private long newCategory() {
        String suffix = String.valueOf(new Random().nextInt(1_000_000_000));
        return categories.save(new Category(1L, "Cat-" + suffix, "slug-" + suffix)).getId();
    }

    @Test
    void managerCreatesDishAndReadsItBack() throws Exception {
        long catId = newCategory();

        mvc.perform(post("/api/dishes")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of(
                                "categoryId", catId,
                                "name", "Pasta fresca",
                                "price", new BigDecimal("12.50"))))
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pasta fresca"))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.category.id").value(catId))
                .andExpect(jsonPath("$.available").value(true));

        long createdId = dishes.findAll().stream()
                .filter(d -> "Pasta fresca".equals(d.getName())).map(Dish::getId).findFirst().orElseThrow();
        mvc.perform(get("/api/dishes/{id}", createdId)
                        .headers(Auth.bearer("kitchen", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pasta fresca"));
    }

    @Test
    void createWithUnknownCategoryIs404() throws Exception {
        mvc.perform(post("/api/dishes")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of(
                                "categoryId", 999_999L,
                                "name", "Sin categoría",
                                "price", new BigDecimal("5.00"))))
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isNotFound());
    }

    @Test
    void managerReplacesDishIngredients() throws Exception {
        long dishId = TestSeed.newDish(categories, dishes);
        long ingId = TestSeed.newIngredient(ingredients);

        mvc.perform(put("/api/dishes/{id}/ingredients", dishId)
                        .contentType("application/json")
                        .content(om.writeValueAsString(List.of(Map.of(
                                "ingredientId", ingId,
                                "quantity", new BigDecimal("200.00"),
                                "unit", "G"))))
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantity").value(200.00))
                .andExpect(jsonPath("$[0].unit").value("G"));
    }

    @Test
    void kitchenCannotCreateADish() throws Exception {
        long catId = newCategory();
        mvc.perform(post("/api/dishes")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of(
                                "categoryId", catId,
                                "name", "Plato prohibido",
                                "price", new BigDecimal("10.00"))))
                        .headers(Auth.bearer("kitchen", users, encoder, jwt)))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotDeleteADishOnlyAdminCan() throws Exception {
        long id = TestSeed.newDish(categories, dishes);
        mvc.perform(delete("/api/dishes/{id}", id)
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isForbidden());
        assertTrue(dishes.findById(id).isPresent(), "El plato debe seguir existiendo");

        mvc.perform(delete("/api/dishes/{id}", id)
                        .headers(Auth.bearer("admin", users, encoder, jwt)))
                .andExpect(status().isNoContent());
        assertTrue(dishes.findById(id).isEmpty(), "El plato debe haberse borrado");
    }

    @Test
    void listAndFilterByCategory() throws Exception {
        long catId = newCategory();
        Dish d = new Dish(1L, catId, "Filtrable", new BigDecimal("8.00"));
        d.setAvailable(Boolean.TRUE);
        dishes.save(d);

        mvc.perform(get("/api/dishes").param("categoryId", String.valueOf(catId))
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Filtrable')]").isNotEmpty());
    }
}
