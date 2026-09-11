package com.cookflow.service;

import com.cookflow.domain.Order;
import com.cookflow.domain.OrderStatus;
import com.cookflow.dto.report.DailyReportRow;
import com.cookflow.dto.report.RangeReportRow;
import com.cookflow.exception.BusinessException;
import com.cookflow.repository.OrderRepository;
import com.cookflow.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.TreeMap;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final OrderRepository orderRepository;

    public ReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<DailyReportRow> daily(LocalDate date) {
        long tenantId = TenantContext.requireTenantId();
        List<Order> orders = paidBetween(tenantId, date.atStartOfDay(), date.atTime(LocalTime.MAX));
        TreeMap<Integer, Bucket> byHour = new TreeMap<>();
        for (Order o : orders) {
            if (o.getCreatedAt() == null) {
                continue;
            }
            byHour.computeIfAbsent(o.getCreatedAt().getHour(), h -> new Bucket()).add(o.getTotal());
        }
        return byHour.entrySet().stream()
                .map(e -> new DailyReportRow(new DailyReportRow.Hour(e.getKey()), e.getValue().count, e.getValue().revenue))
                .toList();
    }

    public List<RangeReportRow> range(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("Rango inválido: 'from' posterior a 'to'");
        }
        long tenantId = TenantContext.requireTenantId();
        List<Order> orders = paidBetween(tenantId, from.atStartOfDay(), to.atTime(LocalTime.MAX));
        TreeMap<LocalDate, Bucket> byDate = new TreeMap<>();
        for (Order o : orders) {
            if (o.getCreatedAt() == null) {
                continue;
            }
            byDate.computeIfAbsent(o.getCreatedAt().toLocalDate(), d -> new Bucket()).add(o.getTotal());
        }
        return byDate.entrySet().stream()
                .map(e -> new RangeReportRow(e.getKey(), (long) e.getValue().count, e.getValue().revenue))
                .toList();
    }

    private List<Order> paidBetween(long tenantId, LocalDateTime from, LocalDateTime to) {
        return orderRepository.findPaidByTenantIdAndCreatedAtBetween(tenantId, OrderStatus.paid, from, to);
    }

    private static final class Bucket {
        private int count;
        private BigDecimal revenue = BigDecimal.ZERO;

        void add(BigDecimal total) {
            count += 1;
            revenue = revenue.add(total == null ? BigDecimal.ZERO : total);
        }
    }
}
