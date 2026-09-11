package com.cookflow.dto.report;

import java.math.BigDecimal;

public record DashboardStats(
        Long todayOrders,
        BigDecimal todayRevenue,
        BigDecimal averageTicket,
        Long openOrders,
        Integer occupiedTables
) {
}
