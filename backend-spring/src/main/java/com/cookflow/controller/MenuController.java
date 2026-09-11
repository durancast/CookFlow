package com.cookflow.controller;

import com.cookflow.dto.category.MenuDto;
import com.cookflow.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menu")
    public MenuDto menu(@RequestParam(required = false) String tenant) {
        return menuService.publicMenu(tenant);
    }
}
