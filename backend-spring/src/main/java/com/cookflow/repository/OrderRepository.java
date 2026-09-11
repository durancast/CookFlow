package com.cookflow.repository;

import com.cookflow.domain.Order;
import com.cookflow.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByTenantId(Long tenantId);

    List<Order> findAllByTenantIdAndStatus(Long tenantId, OrderStatus status);

    Page<Order> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<Order> findAllByTenantIdAndStatus(Long tenantId, OrderStatus status, Pageable pageable);

    /** Pedido abierto de una mesa (pending/preparing/served). */
    @Query("""
            select o from Order o
            where o.tenantId = :tenantId
              and o.diningTableId = :tableId
              and o.status <> :paid
            order by o.id desc
            """)
    Optional<Order> findActiveByTable(@Param("tenantId") Long tenantId,
                                     @Param("tableId") Long tableId,
                                     @Param("paid") OrderStatus paid);

    List<Order> findAllByTenantIdAndStatusIn(Long tenantId, List<OrderStatus> statuses);

    long countByTenantIdAndStatusIn(Long tenantId, List<OrderStatus> statuses);

    @Query("select count(o) from Order o where o.tenantId = :tenantId and o.createdAt >= :since")
    long countByTenantIdAndCreatedAtAfter(@Param("tenantId") Long tenantId, @Param("since") LocalDateTime since);

    @Query("select sum(o.total) from Order o where o.tenantId = :tenantId and o.status = :paid")
    java.math.BigDecimal sumPaidTotalByTenantId(@Param("tenantId") Long tenantId, @Param("paid") OrderStatus paid);

    @Query("select sum(o.total) from Order o where o.tenantId = :tenantId and o.status = :paid and o.createdAt >= :since")
    java.math.BigDecimal sumPaidTotalByTenantIdAndCreatedAtAfter(
            @Param("tenantId") Long tenantId,
            @Param("paid") OrderStatus paid,
            @Param("since") LocalDateTime since);

    @Query("select count(o) from Order o where o.tenantId = :tenantId and o.createdAt >= :from and o.createdAt < :to")
    long countByTenantIdWithin(
            @Param("tenantId") Long tenantId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("select o from Order o where o.tenantId = :tenantId and o.status = :paid and o.createdAt >= :from and o.createdAt < :to")
    List<Order> findPaidByTenantIdAndCreatedAtBetween(
            @Param("tenantId") Long tenantId,
            @Param("paid") OrderStatus paid,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
