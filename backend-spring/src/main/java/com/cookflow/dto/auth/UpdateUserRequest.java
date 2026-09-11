package com.cookflow.dto.auth;

import com.cookflow.domain.UserRole;

public record UpdateUserRequest(
        String name,
        UserRole role
) {
}
