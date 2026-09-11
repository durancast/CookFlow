package com.cookflow;

import com.cookflow.repository.UserRepository;
import com.cookflow.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = CookFlowApplication.class)
@ActiveProfiles("test")
class AuthApiTest extends IntegrationTestBase {

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
    void loginAdminReturnsToken() throws Exception {
        String body = om.writeValueAsString(Map.of(
                "email", TestSeed.ADMIN_EMAIL,
                "password", TestSeed.PASSWORD));
        MvcResult res = mvc.perform(post("/api/auth/login").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType", notNullValue()))
                .andExpect(jsonPath("$.user.role").value("admin"))
                .andReturn();
        JsonNode node = om.readTree(res.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertTrue(node.get("expiresIn").asLong() > 0);
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        String body = om.writeValueAsString(Map.of(
                "email", TestSeed.ADMIN_EMAIL,
                "password", "wrongpass123"));
        mvc.perform(post("/api/auth/login").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void meWithValidTokenSucceeds() throws Exception {
        mvc.perform(get("/api/auth/me").headers(Auth.bearer("admin", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("admin"))
                .andExpect(jsonPath("$.email").value(TestSeed.ADMIN_EMAIL));
    }

    @Test
    void meWithoutTokenIs401() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }
}
