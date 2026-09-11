package com.cookflow.dto.table;

import jakarta.validation.constraints.NotNull;

import com.cookflow.domain.TableStatus;

public record UpdateTableStatusRequest(
        @NotNull TableStatus status
) {
}
