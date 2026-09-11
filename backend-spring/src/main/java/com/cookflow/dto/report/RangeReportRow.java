package com.cookflow.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RangeReportRow(
        LocalDate date,
        Long orders,
        BigDecimal revenue
) {
}
