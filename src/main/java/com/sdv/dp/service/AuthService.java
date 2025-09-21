package com.sdv.dp.service;

import com.sdv.dp.dto.AuthResponse;
import com.sdv.dp.dto.LoginRequest;
import com.sdv.dp.security.JwtService;
import com.sdv.dp.security.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final LoginAttemptService attempts;

    public AuthResponse login(LoginRequest req, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (attempts.isBlocked(req.getEmail(), ip)) {
            throw new LockedException("Too many failed attempts. Try again later.");
        }
        try {
            var auth = new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
            authManager.authenticate(auth);
            UserDetails userDetails = userDetailsService.loadUserByUsername(req.getEmail());
            attempts.recordSuccess(req.getEmail(), ip);
            return AuthResponse.builder().token(jwtService.generate(userDetails)).build();
        } catch (BadCredentialsException ex) {
            attempts.recordFailure(req.getEmail(), ip);
            throw ex;
        }
    }
}
