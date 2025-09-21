package com.sdv.dp.service;

import com.sdv.dp.dto.RegisterRequest;
import com.sdv.dp.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class LoggingUserServiceDecorator implements UserService{
    private static final Logger log = LoggerFactory.getLogger(LoggingUserServiceDecorator.class);
    private final UserService baseUserService;

    @Override
    public User register(RegisterRequest req) {
        log.info("Registering new user: {}", req.getEmail());
        return baseUserService.register(req);
    }

    @Override
    public User getByEmail(String email) {
        log.debug("Fetching user by email {}", email);
        return baseUserService.getByEmail(email);
    }
}
