package com.cookflow.service;

import com.cookflow.domain.DiningTable;
import com.cookflow.domain.Dish;
import com.cookflow.domain.Order;
import com.cookflow.domain.OrderItem;
import com.cookflow.domain.OrderStatus;
import com.cookflow.domain.TableStatus;
import com.cookflow.dto.order.OrderItemDto;
import com.cookflow.dto.table.*;
import com.cookflow.exception.*;
import com.cookflow.repository.DiningTableRepository;
import com.cookflow.repository.DishRepository;
import com.cookflow.repository.OrderItemRepository;
import com.cookflow.repository.OrderRepository;
import com.cookflow.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TableService {

    private final DiningTableRepository tableRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DishRepository dishRepository;

    public TableService(DiningTableRepository tableRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        DishRepository dishRepository) {
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.dishRepository = dishRepository;
    }

    public List<TableDto> list() {
        long tenantId = TenantContext.requireTenantId();
        return tableRepository.findAllByTenantId(tenantId).stream()
                .map(this::toDto)
                .toList();
    }

    public TableDto get(Long id) {
        long tenantId = TenantContext.requireTenantId();
        return toDto(requireTable(tenantId, id));
    }

    public TableDto create(CreateTableRequest req) {
        long tenantId = TenantContext.requireTenantId();
        if (tableRepository.existsByTenantIdAndNumber(tenantId, req.number())) {
            throw new ConflictException("Ya existe una mesa con el número " + req.number());
        }
        DiningTable t = new DiningTable(tenantId, req.number(), req.capacity());
        return toDto(tableRepository.save(t));
    }

    public TableDto updateStatus(Long id, UpdateTableStatusRequest req) {
        long tenantId = TenantContext.requireTenantId();
        DiningTable t = requireTable(tenantId, id);
        TableStatus desired = req.status();
        if (desired == null) {
            throw new BusinessException("status requerido");
        }
        switch (desired) {
            case free -> {
                if (orderRepository.findActiveByTable(tenantId, t.getId(), OrderStatus.paid).isPresent()) {
                    throw new ConflictException("La mesa tiene un pedido activo; no se puede marcar como libre");
                }
                t.setStatus(TableStatus.free);
                t.setWaiterCalled(Boolean.FALSE);
                t.setWaiterCalledAt(null);
            }
            case occupied -> t.setStatus(TableStatus.occupied);
            case pending -> t.setStatus(TableStatus.pending);
        }
        return toDto(tableRepository.save(t));
    }

    public TableDto callWaiter(Long id) {
        long tenantId = TenantContext.requireTenantId();
        DiningTable t = requireTable(tenantId, id);
        t.setWaiterCalled(Boolean.TRUE);
        t.setWaiterCalledAt(LocalDateTime.now());
        if (t.getStatus() == TableStatus.free) {
            t.setStatus(TableStatus.pending);
        }
        return toDto(tableRepository.save(t));
    }

    public TableDto clearWaiter(Long id) {
        long tenantId = TenantContext.requireTenantId();
        DiningTable t = requireTable(tenantId, id);
        t.setWaiterCalled(Boolean.FALSE);
        t.setWaiterCalledAt(null);
        return toDto(tableRepository.save(t));
    }

    public ActiveOrderDto activeOrder(Long id) {
        long tenantId = TenantContext.requireTenantId();
        DiningTable t = requireTable(tenantId, id);
        Order order = orderRepository.findActiveByTable(tenantId, t.getId(), OrderStatus.paid)
                .orElseThrow(() -> new NotFoundException("La mesa " + t.getNumber() + " no tiene pedido activo"));
        return new ActiveOrderDto(order.getId(), order.getStatus().name(), order.getTotal(), itemsOf(order.getId()));
    }

    @Transactional
    public TableDto checkout(Long id) {
        long tenantId = TenantContext.requireTenantId();
        DiningTable t = requireTable(tenantId, id);
        Order order = orderRepository.findActiveByTable(tenantId, t.getId(), OrderStatus.paid)
                .orElseThrow(() -> new NotFoundException("La mesa " + t.getNumber() + " no tiene pedido activo"));
        OrderStatus current = order.getStatus();
        if (!current.canTransitionTo(OrderStatus.paid)) {
            throw new InvalidStateTransitionException(current, OrderStatus.paid);
        }
        order.setStatus(OrderStatus.paid);
        order.setClosedAt(LocalDateTime.now());
        orderRepository.save(order);
        t.setStatus(TableStatus.free);
        t.setWaiterCalled(Boolean.FALSE);
        t.setWaiterCalledAt(null);
        tableRepository.save(t);
        return toDto(t);
    }

    private DiningTable requireTable(long tenantId, Long id) {
        return tableRepository.findById(id)
                .filter(x -> Objects.equals(x.getTenantId(), tenantId))
                .orElseThrow(() -> NotFoundException.resource("Mesa", id));
    }

    private List<OrderItemDto> itemsOf(Long orderId) {
        List<OrderItem> items = orderItemRepository.findAllByOrderId(orderId);
        if (items.isEmpty()) {
            return List.of();
        }
        Set<Long> dishIds = items.stream().map(OrderItem::getDishId).collect(Collectors.toSet());
        Map<Long, Dish> dishes = dishRepository.findAllById(dishIds).stream()
                .collect(Collectors.toMap(Dish::getId, Function.identity()));
        return items.stream()
                .map(oi -> {
                    Dish d = dishes.get(oi.getDishId());
                    return new OrderItemDto(oi.getDishId(),
                            d == null ? null : d.getName(), oi.getQuantity(), oi.getUnitPrice(), oi.getNotes());
                })
                .toList();
    }

    private TableDto toDto(DiningTable t) {
        return new TableDto(t.getId(), t.getNumber(), t.getCapacity(),
                t.getStatus(), t.getWaiterCalled(), t.getWaiterCalledAt());
    }
}
