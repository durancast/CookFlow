package com.cookflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "orders")
public class Order implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "dining_table_id", nullable = false)
    private Long diningTableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dining_table_id", insertable = false, updatable = false)
    private DiningTable diningTable;

    @Column(name = "waiter_id", nullable = false)
    private Long waiterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_id", insertable = false, updatable = false)
    private User waiter;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.pending;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal total = java.math.BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private java.time.LocalDateTime closedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private java.util.List<OrderItem> items = new java.util.ArrayList<>();

    protected Order() {
    }

    public Order(Long tenantId, Long diningTableId, Long waiterId) {
        this.tenantId = tenantId;
        this.diningTableId = diningTableId;
        this.waiterId = waiterId;
    }

    @jakarta.persistence.PrePersist
    void prePersist() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = OrderStatus.pending;
        }
    }

    @jakarta.persistence.PreUpdate
    void preUpdate() {
        updatedAt = java.time.LocalDateTime.now();
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

    public Long getDiningTableId() {
        return diningTableId;
    }

    public void setDiningTableId(Long diningTableId) {
        this.diningTableId = diningTableId;
    }

    public DiningTable getDiningTable() {
        return diningTable;
    }

    public void setDiningTable(DiningTable diningTable) {
        this.diningTable = diningTable;
        if (diningTable != null && diningTable.getId() != null) {
            this.diningTableId = diningTable.getId();
        }
    }

    public Long getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(Long waiterId) {
        this.waiterId = waiterId;
    }

    public User getWaiter() {
        return waiter;
    }

    public void setWaiter(User waiter) {
        this.waiter = waiter;
        if (waiter != null && waiter.getId() != null) {
            this.waiterId = waiter.getId();
        }
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public java.math.BigDecimal getTotal() {
        return total;
    }

    public void setTotal(java.math.BigDecimal total) {
        this.total = total;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public java.time.LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(java.time.LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public java.util.List<OrderItem> getItems() {
        return items;
    }
}
