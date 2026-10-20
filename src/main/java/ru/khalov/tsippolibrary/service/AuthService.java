package ru.khalov.tsippolibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.khalov.tsippolibrary.dto.request.LoginRequest;
import ru.khalov.tsippolibrary.dto.request.RegisterRequest;
import ru.khalov.tsippolibrary.dto.response.AuthResponse;
import ru.khalov.tsippolibrary.entity.RefreshToken;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.repository.UserRepository;
import ru.khalov.tsippolibrary.util.Role;
import ru.khalov.tsippolibrary.util.expeption.UsernameTakenException;

import java.util.Set;

/**
 * Аутентификация: регистрация, логин, refresh, logout (ТЗ 3.1.1, 3.5.3, диаграммы ВИ-5, ВИ-5-alt)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UsernameTakenException(request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            // Если имя уже занято — тоже конфликт (по ТЗ уникален email, но username тоже проверим)
            throw new UsernameTakenException(request.username());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .roles(new java.util.HashSet<>(Set.of(Role.USER)))
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken oldToken = refreshTokenService.verify(rawRefreshToken);
        User user = oldToken.getUser();

        refreshTokenService.revoke(rawRefreshToken);

        String accessToken = jwtService.generateToken(user);
        RefreshToken newRefreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, newRefreshToken.getToken());
    }

    public AuthResponse login(LoginRequest request) {
        // Вход по email (ТЗ 3.1.1)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow(() ->
                new UsernameNotFoundException("User with email " + request.email() + " not found"));

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }
}
