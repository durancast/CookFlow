package com.cookflow;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = CookFlowApplication.class)
@ActiveProfiles("test")
class CatalogApiTest extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper om;

    @BeforeEach
    void seed() {
        TestSeed.seedUsers(users, encoder);
    }

    @Test
    void menuPublicWithoutAuthSucceeds() throws Exception {
        mvc.perform(get("/api/public/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    void dishCreateRequiresManagerOrAdmin() throws Exception {
        String body = om.writeValueAsString(Map.of(
                "name", "Test", "categoryId", 1L, "price", "10.00"));
        mvc.perform(post("/api/dishes")
                        .contentType("application/json").content(body)
                        .headers(Auth.bearer("kitchen", users, encoder, jwt)))
                .andExpect(status().isForbidden());
    }

    @Test
    void dashboardRequiresManagerOrAdmin() throws Exception {
        mvc.perform(get("/api/dashboard/stats").headers(Auth.bearer("waiter", users, encoder, jwt)))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportRequiresManagerOrAdmin() throws Exception {
        mvc.perform(get("/api/reports/daily").param("date", "2026-09-11")
                        .headers(Auth.bearer("kitchen", users, encoder, jwt)))
                .andExpect(status().isForbidden());
    }
}
