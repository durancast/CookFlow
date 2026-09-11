package com.cookflow.dto.report;

import java.math.BigDecimal;

public record DailyReportRow(
        Hour hour,
        Integer orders,
        BigDecimal revenue
) {
    public record Hour(int hour) {
    }
}
