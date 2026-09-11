package com.cookflow.controller;

import com.cookflow.dto.table.ActiveOrderDto;
import com.cookflow.dto.table.CreateTableRequest;
import com.cookflow.dto.table.TableDto;
import com.cookflow.dto.table.UpdateTableStatusRequest;
import com.cookflow.service.TableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_WAITER','ROLE_KITCHEN')")
    public List<TableDto> list() {
        return tableService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_WAITER','ROLE_KITCHEN')")
    public TableDto get(@PathVariable Long id) {
        return tableService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TableDto> create(@Valid @RequestBody CreateTableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tableService.create(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_WAITER')")
    public TableDto updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateTableStatusRequest request) {
        return tableService.updateStatus(id, request);
    }

    @PostMapping("/{id}/call-waiter")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_WAITER')")
    public TableDto callWaiter(@PathVariable Long id) {
        return tableService.callWaiter(id);
    }

    @PostMapping("/{id}/clear-waiter")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_WAITER')")
    public TableDto clearWaiter(@PathVariable Long id) {
        return tableService.clearWaiter(id);
    }

    @GetMapping("/{id}/active-order")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_WAITER','ROLE_KITCHEN')")
    public ActiveOrderDto activeOrder(@PathVariable Long id) {
        return tableService.activeOrder(id);
    }

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_WAITER')")
    public TableDto checkout(@PathVariable Long id) {
        return tableService.checkout(id);
    }
}
