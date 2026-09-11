package com.cookflow.repository;

import com.cookflow.domain.User;
import com.cookflow.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByTenantId(Long tenantId);

    List<User> findAllByTenantIdAndRole(Long tenantId, UserRole role);

    Page<User> findAllByTenantId(Long tenantId, Pageable pageable);

    List<User> findByTenantIdAndEmailIgnoreCase(Long tenantId, String email);
}
