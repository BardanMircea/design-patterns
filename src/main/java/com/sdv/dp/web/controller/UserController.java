package com.sdv.dp.web.controller;

import com.sdv.dp.dto.UserDTO;
import com.sdv.dp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserDTO me(@AuthenticationPrincipal UserDetails principal) {
        var u = userService.getByEmail(principal.getUsername());
        return UserDTO.builder().id(u.getId()).email(u.getEmail()).build();
    }
}
