package com.cookflow.security;

import com.cookflow.domain.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Resuelve el tenant y usuario autenticado de la request.
 * Estrategia actual: tenant = tenant_id del usuario en el JWT.
 * Evolución prevista: override opcional por header X-Tenant-Id.
 */
public final class TenantContext {

    private TenantContext() {
    }

    public static Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public static Optional<Long> currentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return Optional.ofNullable(user.getTenantId());
        }
        // El principal también puede venir como map con claim "tenantId".
        if (principal instanceof java.util.Map<?, ?> claims) {
            Object v = claims.get("tenantId");
            return v == null ? Optional.empty() : Optional.of(((Number) v).longValue());
        }
        return Optional.empty();
    }

    public static long requireTenantId() {
        return currentTenantId()
                .orElseThrow(() -> new com.cookflow.exception.TenantRequiredException());
    }
}
