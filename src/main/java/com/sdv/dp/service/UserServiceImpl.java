package com.sdv.dp.service;

import com.sdv.dp.dto.RegisterRequest;
import com.sdv.dp.model.Role;
import com.sdv.dp.model.User;
import com.sdv.dp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service("baseUserService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Override @Transactional
    public User register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        var user = User.builder()
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }
}
