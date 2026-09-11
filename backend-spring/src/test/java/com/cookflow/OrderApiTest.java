package com.cookflow;

import com.cookflow.domain.TableStatus;
import com.cookflow.repository.CategoryRepository;
import com.cookflow.repository.DiningTableRepository;
import com.cookflow.repository.DishRepository;
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

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = CookFlowApplication.class)
@ActiveProfiles("test")
class OrderApiTest extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper om;
    @Autowired DiningTableRepository tables;
    @Autowired CategoryRepository categories;
    @Autowired DishRepository dishes;

    private long tableId;
    private long dishId;

    @BeforeEach
    void seed() {
        TestSeed.seedUsers(users, encoder);
        tableId = TestSeed.newTable(tables);
        dishId = TestSeed.newDish(categories, dishes);
    }

    private MvcResult createOrder(String role) throws Exception {
        Map<String, Object> body = Map.of(
                "tableId", tableId,
                "items", List.of(Map.of("dishId", dishId, "quantity", 2)));
        return mvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(om.writeValueAsString(body))
                        .headers(Auth.bearer(role, users, encoder, jwt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"))
                .andReturn();
    }

    private JsonNode body(MvcResult r) throws Exception {
        return om.readTree(r.getResponse().getContentAsString());
    }

    @Test
    void createOrderSetsTableOccupied() throws Exception {
        createOrder("waiter");
        org.junit.jupiter.api.Assertions.assertEquals(TableStatus.occupied,
                tables.findById(tableId).orElseThrow().getStatus());
    }

    @Test
    void sameTableCannotHaveTwoActiveOrders() throws Exception {
        createOrder("waiter");
        mvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(om.writeValueAsString(Map.of(
                                "tableId", tableId,
                                "items", List.of(Map.of("dishId", dishId, "quantity", 1)))))
                        .headers(Auth.bearer("waiter", users, encoder, jwt)))
                .andExpect(status().isConflict());
    }

    @Test
    void happyPathTransitionToEnd() throws Exception {
        long orderId = body(createOrder("waiter")).get("id").asLong();
        mvc.perform(patch("/api/orders/{id}/status", orderId).headers(Auth.bearer("waiter", users, encoder, jwt))
                        .contentType("application/json").content("{\"status\":\"preparing\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("preparing"));

        mvc.perform(patch("/api/orders/{id}/status", orderId).headers(Auth.bearer("waiter", users, encoder, jwt))
                        .contentType("application/json").content("{\"status\":\"served\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("served"));

        mvc.perform(patch("/api/orders/{id}/status", orderId).headers(Auth.bearer("manager", users, encoder, jwt))
                        .contentType("application/json").content("{\"status\":\"paid\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("paid"));
    }

    @Test
    void paidTransitionFreesTheTable() throws Exception {
        long orderId = body(createOrder("waiter")).get("id").asLong();
        mvc.perform(patch("/api/orders/{id}/status", orderId).headers(Auth.bearer("waiter", users, encoder, jwt))
                        .contentType("application/json").content("{\"status\":\"preparing\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/orders/{id}/status", orderId).headers(Auth.bearer("waiter", users, encoder, jwt))
                        .contentType("application/json").content("{\"status\":\"served\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/orders/{id}/status", orderId).headers(Auth.bearer("manager", users, encoder, jwt))
                        .contentType("application/json").content("{\"status\":\"paid\"}"))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(TableStatus.free,
                tables.findById(tableId).orElseThrow().getStatus());
    }

    @Test
    void kitchenCannotTransitionToPaid() throws Exception {
        long orderId = body(createOrder("waiter")).get("id").asLong();
        mvc.perform(patch("/api/orders/{id}/status", orderId)
                        .headers(Auth.bearer("kitchen", users, encoder, jwt))
                        .contentType("application/json").content("{\"status\":\"paid\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void skippingAStatusIsRejected() throws Exception {
        long orderId = body(createOrder("waiter")).get("id").asLong();
        mvc.perform(patch("/api/orders/{id}/status", orderId)
                        .headers(Auth.bearer("waiter", users, encoder, jwt))
                        .contentType("application/json").content("{\"status\":\"served\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void kitchenSeesDefaultPendingOrdersButNotPaid() throws Exception {
        createOrder("waiter");
        mvc.perform(get("/api/orders").headers(Auth.bearer("kitchen", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(greaterThan(0)));

        mvc.perform(get("/api/orders").param("status", "paid")
                        .headers(Auth.bearer("kitchen", users, encoder, jwt)))
                .andExpect(status().isBadRequest());
    }
}
