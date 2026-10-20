package ru.khalov.tsippolibrary.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.repository.UserRepository;
import ru.khalov.tsippolibrary.util.Role;

import java.util.Set;

/**
 * Инициализация OWNER при первом запуске (ТЗ 3.1.5):
 * учётка владельца создаётся из конфигурации, регистрация OWNER невозможна.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${library.owner.username:owner}")
    private String ownerUsername;

    @Value("${library.owner.email:owner@library.local}")
    private String ownerEmail;

    @Value("${library.owner.password:owner123}")
    private String ownerPassword;

    @Bean
    public ApplicationRunner initOwner() {
        return args -> {
            // Исправлено: не грузим всю таблицу (ТЗ 3.4.4)
            boolean ownerExists = userRepository.existsByRolesContaining(Role.OWNER);
            if (ownerExists) {
                log.info("OWNER already exists, skipping init");
                return;
            }
            if (userRepository.existsByEmail(ownerEmail)) {
                log.warn("User with owner email already exists, not creating OWNER");
                return;
            }
            User owner = User.builder()
                    .username(ownerUsername)
                    .email(ownerEmail)
                    .password(passwordEncoder.encode(ownerPassword))
                    .enabled(true)
                    .roles(Set.of(Role.OWNER, Role.ADMIN, Role.USER))
                    .build();
            userRepository.save(owner);
            log.info("OWNER created: email={}, username={}", ownerEmail, ownerUsername);
        };
    }
}
