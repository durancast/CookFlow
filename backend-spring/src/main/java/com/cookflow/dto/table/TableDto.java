package com.cookflow.dto.table;

import com.cookflow.domain.TableStatus;

import java.time.LocalDateTime;

public record TableDto(
        Long id,
        Integer number,
        Integer capacity,
        TableStatus status,
        Boolean waiterCalled,
        LocalDateTime waiterCalledAt
) {
}
