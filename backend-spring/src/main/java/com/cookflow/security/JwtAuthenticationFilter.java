package com.cookflow.security;

import com.cookflow.domain.User;
import com.cookflow.domain.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(7);
            if (jwtUtil.isValid(token)) {
                try {
                    Claims claims = jwtUtil.parse(token);
                    // Principal: un User inmutable con los datos del claim.
                    User principal = new User(
                            claims.get("tenantId", Long.class),
                            claims.get("name", String.class),
                            claims.get("email", String.class),
                            null,
                            UserRole.valueOf(claims.get("role", String.class))
                    );
                    if (claims.getSubject() != null) {
                        principal.setId(Long.valueOf(claims.getSubject()));
                    }
                    GrantedAuthority authority = new SimpleGrantedAuthority(
                            "ROLE_" + claims.get("role", String.class).toUpperCase());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                catch (IllegalArgumentException ignored) {
                    // claims malformados: no se autentica
                }
            }
        }
        chain.doFilter(request, response);
    }
}
