package com.cookflow.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.cookflow.domain.UserRole;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 128) String password,
        @NotBlank @Size(max = 100) String name,
        UserRole role
) {
}
