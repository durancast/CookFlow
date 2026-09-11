package com.cookflow;

import com.cookflow.repository.DiningTableRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = CookFlowApplication.class)
@ActiveProfiles("test")
class TableApiTest extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper om;
    @Autowired DiningTableRepository tables;

    @BeforeEach
    void seed() {
        TestSeed.seedUsers(users, encoder);
    }

    @Test
    void adminCreatesTable() throws Exception {
        long id = TestSeed.newTable(tables);
        int number = tables.findById(id).orElseThrow().getNumber();

        int capacity = tables.findById(id).orElseThrow().getCapacity();

        // La mesa ya existe, pero la creamos vía API con un número nuevo
        int newNumber = nextFreeNumber();
        mvc.perform(post("/api/tables")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of("number", newNumber, "capacity", 6)))
                        .headers(Auth.bearer("admin", users, encoder, jwt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value(newNumber))
                .andExpect(jsonPath("$.capacity").value(6))
                .andExpect(jsonPath("$.status").value("free"));

        assertTrue(tables.existsByTenantIdAndNumber(1L, newNumber), "La mesa debe existir");
    }

    @Test
    void waiterCannotCreateTable() throws Exception {
        mvc.perform(post("/api/tables")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of("number", 99999, "capacity", 4)))
                        .headers(Auth.bearer("waiter", users, encoder, jwt)))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateTableNumberIsConflict() throws Exception {
        int num = nextFreeNumber();
        assertTrue(tables.save(new com.cookflow.domain.DiningTable(1L, num, 2)) != null);

        mvc.perform(post("/api/tables")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of("number", num, "capacity", 4)))
                        .headers(Auth.bearer("admin", users, encoder, jwt)))
                .andExpect(status().isConflict());
    }

    @Test
    void callWaiterSetsFlagAndClearWaiterResetsIt() throws Exception {
        long id = TestSeed.newTable(tables);

        mvc.perform(post("/api/tables/{id}/call-waiter", id)
                        .headers(Auth.bearer("waiter", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiterCalled").value(true));
        assertTrue(tables.findById(id).orElseThrow().getWaiterCalled());

        mvc.perform(post("/api/tables/{id}/clear-waiter", id)
                        .headers(Auth.bearer("waiter", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiterCalled").value(false));
        assertFalse(tables.findById(id).orElseThrow().getWaiterCalled());
    }

    @Test
    void updateStatusToOccupied() throws Exception {
        long id = TestSeed.newTable(tables);

        mvc.perform(patch("/api/tables/{id}/status", id)
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of("status", "occupied")))
                        .headers(Auth.bearer("waiter", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("occupied"));
        assertTrue("occupied".equals(tables.findById(id).orElseThrow().getStatus().name()));
    }

    @Test
    void kitchenCannotCallWaiter() throws Exception {
        long id = TestSeed.newTable(tables);
        mvc.perform(post("/api/tables/{id}/call-waiter", id)
                        .headers(Auth.bearer("kitchen", users, encoder, jwt)))
                .andExpect(status().isForbidden());
    }

    private int nextFreeNumber() {
        int n = 10_000 + (int)(Math.random() * 89_999);
        while (tables.existsByTenantIdAndNumber(1L, n)) {
            n += 1;
        }
        return n;
    }
}
