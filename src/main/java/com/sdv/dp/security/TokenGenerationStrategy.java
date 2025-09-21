package com.sdv.dp.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface TokenGenerationStrategy {
    String generate(UserDetails user);
    boolean isValid(String token, UserDetails user);
    String extractUsername(String token);
}
