package com.cookflow;

import com.cookflow.domain.IngredientUnit;
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
class IngredientApiTest extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper om;
    @Autowired IngredientRepository ingredients;

    @BeforeEach
    void seed() {
        TestSeed.seedUsers(users, encoder);
    }

    private String newName() {
        return "Ing-" + new Random().nextInt(1_000_000);
    }

    @Test
    void managerCreatesIngredient() throws Exception {
        long id = TestSeed.newIngredient(ingredients);
        String name = ingredients.findById(id).orElseThrow().getName();

        mvc.perform(post("/api/ingredients")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of("name", newName(), "defaultUnit", "KG")))
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").isNotEmpty())
                .andExpect(jsonPath("$.defaultUnit").value("KG"));
    }

    @Test
    void duplicateNameIsConflict() throws Exception {
        long id = TestSeed.newIngredient(ingredients);
        String name = ingredients.findById(id).orElseThrow().getName();

        mvc.perform(post("/api/ingredients")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of("name", name, "defaultUnit", "G")))
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateChangesNameAndUnit() throws Exception {
        long id = TestSeed.newIngredient(ingredients);
        String newName = newName();

        mvc.perform(put("/api/ingredients/{id}", id)
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of("name", newName, "defaultUnit", "L")))
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.defaultUnit").value("L"));
    }

    @Test
    void kitchenReadsList() throws Exception {
        TestSeed.newIngredient(ingredients);
        mvc.perform(get("/api/ingredients")
                        .headers(Auth.bearer("kitchen", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void waiterCannotCreate() throws Exception {
        mvc.perform(post("/api/ingredients")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of("name", newName(), "defaultUnit", "G")))
                        .headers(Auth.bearer("waiter", users, encoder, jwt)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteRequiresAdmin() throws Exception {
        long id = TestSeed.newIngredient(ingredients);
        mvc.perform(delete("/api/ingredients/{id}", id)
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/api/ingredients/{id}", id)
                        .headers(Auth.bearer("admin", users, encoder, jwt)))
                .andExpect(status().isNoContent());
        assertTrue(ingredients.findById(id).isEmpty());
    }
}
