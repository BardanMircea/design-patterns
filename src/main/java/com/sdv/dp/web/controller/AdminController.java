package com.sdv.dp.web.controller;

import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/panel")
    public String adminOnly() {
        return "Welcome, admin!";
    }
}
