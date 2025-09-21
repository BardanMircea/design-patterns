package com.sdv.dp.service;

import com.sdv.dp.security.TokenGenerationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final TokenGenerationStrategy strategy;
    public String generate(UserDetails user) { return strategy.generate(user); }
    public String extractUsername(String token) { return strategy.extractUsername(token); }
    public boolean isValid(String token, UserDetails user) { return strategy.isValid(token, user); }
}
