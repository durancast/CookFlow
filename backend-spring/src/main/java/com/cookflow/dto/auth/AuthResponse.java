package com.cookflow.dto.auth;

public record AuthResponse(
        String token,
        long expiresIn,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse bearer(String token, long expiresIn, UserResponse user) {
        return new AuthResponse(token, expiresIn, "Bearer", user);
    }
}
