package com.cookflow.dto.auth;

import com.cookflow.domain.UserRole;

import java.time.LocalDateTime;

public record UserDetailResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        Long tenantId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
