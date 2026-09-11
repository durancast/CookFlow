package com.cookflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "dining_tables")
public class DiningTable implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private TableStatus status = TableStatus.free;

    @Column(name = "waiter_called", nullable = false)
    private Boolean waiterCalled = Boolean.FALSE;

    @Column(name = "waiter_called_at")
    private LocalDateTime waiterCalledAt;

    protected DiningTable() {
    }

    public DiningTable(Long tenantId, Integer number, Integer capacity) {
        this.tenantId = tenantId;
        this.number = number;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public TableStatus getStatus() {
        return status;
    }

    public void setStatus(TableStatus status) {
        this.status = status;
    }

    public Boolean getWaiterCalled() {
        return waiterCalled;
    }

    public void setWaiterCalled(Boolean waiterCalled) {
        this.waiterCalled = waiterCalled;
    }

    public LocalDateTime getWaiterCalledAt() {
        return waiterCalledAt;
    }

    public void setWaiterCalledAt(LocalDateTime waiterCalledAt) {
        this.waiterCalledAt = waiterCalledAt;
    }
}
