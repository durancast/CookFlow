package com.cookflow.security;

import com.cookflow.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final String issuer;
    private final long expirationSeconds;

    public JwtUtil(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-seconds}") long expirationSeconds
    ) {
        byte[] decoded = Base64.getDecoder().decode(base64Secret);
        // AES-256 requiere >= 32 bytes. Si el secreto es corto, se rellena.
        if (decoded.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(decoded, 0, padded, 0, Math.min(decoded.length, 32));
            decoded = padded;
        }
        this.key = Keys.hmacShaKeyFor(decoded);
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(issuer)
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .claim("tenantId", user.getTenantId())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Devuelve true si el token es válido y no expirado. */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        }
        catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
