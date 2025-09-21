package com.sdv.dp.config;

import com.sdv.dp.model.Role;
import com.sdv.dp.model.User;
import com.sdv.dp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        users.findByEmail("admin@email.com").orElseGet(() -> users.save(
                User.builder()
                        .email("admin@email.com")
                        .password(encoder.encode("AdminPassword1!"))
                        .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                        .enabled(true)
                        .build()
        ));
    }
}
