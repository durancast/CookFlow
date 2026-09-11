package com.cookflow.service;

import com.cookflow.domain.OrderStatus;
import com.cookflow.domain.TableStatus;
import com.cookflow.dto.report.DashboardStats;
import com.cookflow.repository.DiningTableRepository;
import com.cookflow.repository.OrderRepository;
import com.cookflow.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final DiningTableRepository tableRepository;

    public DashboardService(OrderRepository orderRepository, DiningTableRepository tableRepository) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
    }

    public DashboardStats stats() {
        long tenantId = TenantContext.requireTenantId();
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        long todayOrders = orderRepository.countByTenantIdWithin(tenantId, start, end);
        BigDecimal todayRevenue = nullSafe(
                orderRepository.sumPaidTotalByTenantIdAndCreatedAtAfter(tenantId, OrderStatus.paid, start));
        BigDecimal averageTicket = todayOrders == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : todayRevenue.divide(BigDecimal.valueOf(todayOrders), 2, RoundingMode.HALF_UP);
        long openOrders = orderRepository.countByTenantIdAndStatusIn(
                tenantId, List.of(OrderStatus.pending, OrderStatus.preparing, OrderStatus.served));
        long occupiedTables = tableRepository.countByTenantIdAndStatus(tenantId, TableStatus.occupied);
        return new DashboardStats(todayOrders, todayRevenue, averageTicket, openOrders, (int) occupiedTables);
    }

    private static BigDecimal nullSafe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
