package com.cookflow.repository;

import com.cookflow.domain.DiningTable;
import com.cookflow.domain.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {

    List<DiningTable> findAllByTenantId(Long tenantId);

    Optional<DiningTable> findByTenantIdAndNumber(Long tenantId, Integer number);

    Optional<DiningTable> findByTenantIdAndId(Long tenantId, Long id);

    boolean existsByTenantIdAndNumber(Long tenantId, Integer number);

    long countByTenantIdAndStatus(Long tenantId, TableStatus status);
}
