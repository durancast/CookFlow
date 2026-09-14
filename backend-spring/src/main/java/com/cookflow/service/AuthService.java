package com.cookflow.service;

import com.cookflow.domain.User;
import com.cookflow.dto.auth.AuthResponse;
import com.cookflow.dto.auth.LoginRequest;
import com.cookflow.dto.auth.UserResponse;
import com.cookflow.exception.BusinessException;
import com.cookflow.repository.UserRepository;
import com.cookflow.security.JwtUtil;
import com.cookflow.security.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));
        String token = jwtUtil.generateToken(user);
        return AuthResponse.bearer(token, jwtUtil.getExpirationSeconds(), toUserResponse(user));
    }

    public UserResponse me() {
        User user = TenantContext.currentUser()
                .map(u -> userRepository.findById(u.getId()).orElseThrow())
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
        return toUserResponse(user);
    }

    /**
     * Listado de staff para el dropdown de login.
     * Público por diseño: el login aún no tiene token.
     * Siempre filtra por tenantId para no revelar usuarios de otros
     * restaurantes.
     */
    public List<UserResponse> staff(long tenantId) {
        return userRepository.findAllByTenantId(tenantId).stream()
                .map(AuthService::toUserResponse)
                .toList();
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getTenantId());
    }
}
