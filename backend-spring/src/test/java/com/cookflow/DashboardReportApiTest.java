package com.cookflow;

import com.cookflow.domain.Order;
import com.cookflow.domain.OrderStatus;
import com.cookflow.repository.DiningTableRepository;
import com.cookflow.repository.OrderRepository;
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
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = CookFlowApplication.class)
@ActiveProfiles("test")
class DashboardReportApiTest extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper om;
    @Autowired DiningTableRepository tables;
    @Autowired OrderRepository orders;

    @BeforeEach
    void seed() {
        TestSeed.seedUsers(users, encoder);
    }

    /** Crear un pedido pagado (hoy) sobre una mesa fresca. */
    private void seedPaidOrder(String total) {
        long tableId = TestSeed.newTable(tables);
        long waiterId = users.findByEmail(TestSeed.WAITER_EMAIL).orElseThrow().getId();
        Order order = new Order(1L, tableId, waiterId);
        order.setStatus(OrderStatus.paid);
        order.setTotal(new BigDecimal(total));
        order.setClosedAt(java.time.LocalDateTime.now());
        orders.save(order);
    }

    @Test
    void dashboardReturnsStats() throws Exception {
        seedPaidOrder("45.00");

        mvc.perform(get("/api/dashboard/stats")
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayOrders").isNumber())
                .andExpect(jsonPath("$.todayRevenue").isNumber())
                .andExpect(jsonPath("$.averageTicket").isNumber())
                .andExpect(jsonPath("$.openOrders").isNumber())
                .andExpect(jsonPath("$.occupiedTables").isNumber());
    }

    @Test
    void waiterCannotSeeDashboard() throws Exception {
        mvc.perform(get("/api/dashboard/stats")
                        .headers(Auth.bearer("waiter", users, encoder, jwt)))
                .andExpect(status().isForbidden());
    }

    @Test
    void dailyReportContainsTodayBucket() throws Exception {
        seedPaidOrder("30.00");
        LocalDate today = LocalDate.now();

        mvc.perform(get("/api/reports/daily").param("date", today.toString())
                        .headers(Auth.bearer("admin", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.revenue > 0)]").isNotEmpty());
    }

    @Test
    void rangeReportSpansDates() throws Exception {
        seedPaidOrder("20.00");
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(2);
        LocalDate to = today;

        mvc.perform(get("/api/reports/range")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .headers(Auth.bearer("manager", users, encoder, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.revenue > 0)]").isNotEmpty());
    }

    @Test
    void invalidRangeIs400() throws Exception {
        LocalDate today = LocalDate.now();
        mvc.perform(get("/api/reports/range")
                        .param("from", today.toString())
                        .param("to", today.minusDays(1).toString())
                        .headers(Auth.bearer("admin", users, encoder, jwt)))
                .andExpect(status().isBadRequest());
    }
}
