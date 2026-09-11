package com.cookflow;

import com.cookflow.domain.User;
import com.cookflow.domain.UserRole;
import com.cookflow.repository.UserRepository;
import com.cookflow.security.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

/**
 * Genera headers autenticados con un Bearer real para cada rol (usando JwtUtil).
 */
public final class Auth {

    private Auth() {
    }

    public static HttpHeaders bearer(String role, UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
        UserRole roleEnum = UserRole.valueOf(role.toLowerCase(Locale.ROOT));
        User user = users.findByEmail(emailOf(roleEnum))
                .orElseGet(() -> users.save(new User(1L, "test-" + role, emailOf(roleEnum),
                        encoder.encode(TestSeed.PASSWORD), roleEnum)));
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "Bearer " + jwt.generateToken(user));
        return h;
    }

    private static String emailOf(UserRole r) {
        return switch (r) {
            case admin -> TestSeed.ADMIN_EMAIL;
            case waiter -> TestSeed.WAITER_EMAIL;
            case kitchen -> TestSeed.KITCHEN_EMAIL;
            case manager -> TestSeed.MANAGER_EMAIL;
        };
    }
}
