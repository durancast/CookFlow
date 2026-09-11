package com.cookflow.dto.auth;

import com.cookflow.domain.UserRole;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        Long tenantId
) {
}
