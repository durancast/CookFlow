package com.cookflow.service;

import com.cookflow.domain.DiningTable;
import com.cookflow.domain.Dish;
import com.cookflow.domain.Order;
import com.cookflow.domain.OrderItem;
import com.cookflow.domain.OrderStatus;
import com.cookflow.domain.TableStatus;
import com.cookflow.domain.User;
import com.cookflow.domain.UserRole;
import com.cookflow.dto.order.CreateOrderRequest;
import com.cookflow.dto.order.OrderDetailDto;
import com.cookflow.dto.order.OrderItemDto;
import com.cookflow.dto.order.OrderSummaryDto;
import com.cookflow.dto.order.UpdateOrderStatusRequest;
import com.cookflow.exception.BusinessException;
import com.cookflow.exception.ConflictException;
import com.cookflow.exception.NotFoundException;
import com.cookflow.security.TenantContext;
import com.cookflow.repository.DiningTableRepository;
import com.cookflow.repository.DishRepository;
import com.cookflow.repository.OrderItemRepository;
import com.cookflow.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DishRepository dishRepository;
    private final DiningTableRepository tableRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        DishRepository dishRepository,
                        DiningTableRepository tableRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.dishRepository = dishRepository;
        this.tableRepository = tableRepository;
    }

    public Page<OrderSummaryDto> list(OrderStatus statusFilter, Pageable pageable) {
        long tenantId = TenantContext.requireTenantId();
        if (currentRole() == UserRole.kitchen) {
            if (statusFilter != null && statusFilter == OrderStatus.paid) {
                throw new BusinessException("KITCHEN no puede ver pedidos pagados");
            }
            if (statusFilter == null) {
                statusFilter = OrderStatus.pending;
            }
        }
        Page<Order> page = (statusFilter == null)
                ? orderRepository.findAllByTenantId(tenantId, pageable)
                : orderRepository.findAllByTenantIdAndStatus(tenantId, statusFilter, pageable);
        return page.map(this::toSummary);
    }

    public OrderDetailDto get(Long id) {
        long tenantId = TenantContext.requireTenantId();
        Order order = requireOrder(tenantId, id);
        if (currentRole() == UserRole.kitchen && !kitchenCanSee(order.getStatus())) {
            throw new NotFoundException("Pedido no visible para KITCHEN");
        }
        return toDetail(tenantId, order);
    }

    @Transactional
    public OrderDetailDto create(CreateOrderRequest req) {
        long tenantId = TenantContext.requireTenantId();
        Long waiterId = TenantContext.currentUser().map(User::getId)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
        DiningTable table = tableRepository.findByTenantIdAndId(tenantId, req.tableId())
                .orElseThrow(() -> NotFoundException.resource("Mesa", req.tableId()));
        orderRepository.findActiveByTable(tenantId, table.getId(), OrderStatus.paid)
                .ifPresent(o -> {
                    throw new ConflictException("La mesa " + table.getNumber() + " ya tiene un pedido activo (pedido " + o.getId() + ")");
                });

        List<Long> dishIds = req.items().stream().map(it -> it.dishId()).toList();
        List<Dish> dishes = dishRepository.findAllById(dishIds);
        if (dishes.size() != new java.util.HashSet<>(dishIds).size()) {
            throw new BusinessException("Hay platos duplicados en el pedido");
        }
        for (Dish dish : dishes) {
            if (dish == null) {
                throw NotFoundException.resource("Plato", dishIds);
            }
            if (!Objects.equals(dish.getTenantId(), tenantId)) {
                throw NotFoundException.resource("Plato", dish.getId());
            }
            if (!Boolean.TRUE.equals(dish.getAvailable())) {
                throw new ConflictException("El plato '" + dish.getName() + "' no está disponible");
            }
        }

        Order order = new Order(tenantId, table.getId(), waiterId);
        BigDecimal total = BigDecimal.ZERO;
        for (var it : req.items()) {
            Dish dish = dishRepository.findById(it.dishId()).orElseThrow(
                    () -> NotFoundException.resource("Plato", it.dishId()));
            total = total.add(dish.getPrice().multiply(BigDecimal.valueOf(it.quantity())));
        }
        order.setTotal(total);
        Order savedOrder = orderRepository.save(order);

        for (var it : req.items()) {
            Dish dish = dishRepository.findById(it.dishId()).orElseThrow(
                    () -> NotFoundException.resource("Plato", it.dishId()));
            orderItemRepository.save(new OrderItem(
                    savedOrder.getId(), dish.getId(), it.quantity(), dish.getPrice(), it.notes()));
        }

        table.setStatus(TableStatus.occupied);
        tableRepository.save(table);
        return toDetail(tenantId, savedOrder);
    }

    @Transactional
    public OrderDetailDto transition(Long id, UpdateOrderStatusRequest req) {
        long tenantId = TenantContext.requireTenantId();
        Order order = requireOrder(tenantId, id);
        OrderStatus next = req.status();
        if (next == null) {
            throw new BusinessException("status requerido");
        }
        if (currentRole() == UserRole.kitchen && next == OrderStatus.paid) {
            throw new BusinessException("KITCHEN no puede cobrar el pedido");
        }
        if (!order.getStatus().canTransitionTo(next)) {
            throw new com.cookflow.exception.InvalidStateTransitionException(order.getStatus(), next);
        }
        order.setStatus(next);
        if (next == OrderStatus.paid) {
            order.setClosedAt(LocalDateTime.now());
        }
        Order saved = orderRepository.save(order);

        if (next == OrderStatus.paid) {
            tableRepository.findByTenantIdAndId(tenantId, saved.getDiningTableId()).ifPresent(t -> {
                t.setStatus(TableStatus.free);
                t.setWaiterCalled(Boolean.FALSE);
                t.setWaiterCalledAt(null);
                tableRepository.save(t);
            });
        }
        return toDetail(tenantId, saved);
    }

    private boolean kitchenCanSee(OrderStatus status) {
        return status == OrderStatus.pending || status == OrderStatus.preparing;
    }

    private UserRole currentRole() {
        return TenantContext.currentUser().map(User::getRole).orElse(UserRole.kitchen);
    }

    private Order requireOrder(long tenantId, Long id) {
        return orderRepository.findById(id)
                .filter(o -> Objects.equals(o.getTenantId(), tenantId))
                .orElseThrow(() -> NotFoundException.resource("Pedido", id));
    }

    private OrderSummaryDto toSummary(Order o) {
        return new OrderSummaryDto(
                o.getId(),
                tableNumber(o.getDiningTableId()),
                o.getStatus(),
                o.getTotal(),
                o.getCreatedAt());
    }

    private OrderDetailDto toDetail(long tenantId, Order o) {
        Integer number = tableNumber(o.getDiningTableId());
        List<OrderItemDto> items = itemsOf(o.getId(), tenantId);
        return new OrderDetailDto(o.getId(), number, o.getStatus(), o.getTotal(), o.getCreatedAt(), items);
    }

    private Integer tableNumber(Long tableId) {
        if (tableId == null) {
            return null;
        }
        return tableRepository.findById(tableId).map(DiningTable::getNumber).orElse(null);
    }

    private List<OrderItemDto> itemsOf(Long orderId, long tenantId) {
        List<OrderItem> items = orderItemRepository.findAllByOrderId(orderId);
        if (items.isEmpty()) {
            return List.of();
        }
        Set<Long> dishIds = items.stream().map(OrderItem::getDishId).collect(Collectors.toSet());
        Map<Long, Dish> dishes = dishRepository.findAllById(dishIds).stream()
                .filter(d -> Objects.equals(d.getTenantId(), tenantId))
                .collect(Collectors.toMap(Dish::getId, Function.identity()));
        return items.stream()
                .map(oi -> {
                    Dish d = dishes.get(oi.getDishId());
                    return new OrderItemDto(oi.getDishId(),
                            d == null ? null : d.getName(),
                            oi.getQuantity(), oi.getUnitPrice(), oi.getNotes());
                })
                .toList();
    }
}
