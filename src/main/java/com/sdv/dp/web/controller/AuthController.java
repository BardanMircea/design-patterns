package com.sdv.dp.web.controller;

import com.sdv.dp.dto.AuthResponse;
import com.sdv.dp.dto.LoginRequest;
import com.sdv.dp.dto.RegisterRequest;
import com.sdv.dp.dto.UserDTO;
import com.sdv.dp.service.AuthService;
import com.sdv.dp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest req) {
        var user = userService.register(req);
        return ResponseEntity.ok(UserDTO.builder().id(user.getId()).email(user.getEmail()).build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(authService.login(req, http));
    }
}
