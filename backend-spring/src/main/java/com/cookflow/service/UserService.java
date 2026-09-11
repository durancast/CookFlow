package com.cookflow.service;

import com.cookflow.domain.User;
import com.cookflow.domain.UserRole;
import com.cookflow.dto.auth.CreateUserRequest;
import com.cookflow.dto.auth.UpdateUserRequest;
import com.cookflow.dto.auth.UserDetailResponse;
import com.cookflow.dto.auth.UserResponse;
import com.cookflow.exception.ConflictException;
import com.cookflow.exception.NotFoundException;
import com.cookflow.repository.UserRepository;
import com.cookflow.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(CreateUserRequest req) {
        long tenantId = TenantContext.requireTenantId();
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Ya existe un usuario con el email " + req.email());
        }
        UserRole role = req.role() == null ? UserRole.waiter : req.role();
        User user = new User(tenantId, req.name(), req.email(),
                passwordEncoder.encode(req.password()), role);
        return toResponse(userRepository.save(user));
    }

    public Page<UserResponse> all(Pageable pageable) {
        long tenantId = TenantContext.requireTenantId();
        return userRepository.findAllByTenantId(tenantId, pageable)
                .map(AuthService::toUserResponse);
    }

    public UserDetailResponse get(Long id) {
        long tenantId = TenantContext.requireTenantId();
        User u = requireUser(id, tenantId);
        return new UserDetailResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(),
                u.getTenantId(), u.getCreatedAt(), u.getUpdatedAt());
    }

    public UserResponse update(Long id, UpdateUserRequest req) {
        long tenantId = TenantContext.requireTenantId();
        User u = requireUser(id, tenantId);
        if (req.name() != null && !req.name().isBlank()) {
            u.setName(req.name());
        }
        if (req.role() != null) {
            u.setRole(req.role());
        }
        return toResponse(userRepository.save(u));
    }

    @Transactional
    public void delete(Long id) {
        long tenantId = TenantContext.requireTenantId();
        User u = requireUser(id, tenantId);
        userRepository.delete(u);
    }

    private User requireUser(Long id, long tenantId) {
        return userRepository.findById(id)
                .filter(u -> Objects.equals(u.getTenantId(), tenantId))
                .orElseThrow(() -> NotFoundException.resource("Usuario", id));
    }

    private static UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getTenantId());
    }
}
