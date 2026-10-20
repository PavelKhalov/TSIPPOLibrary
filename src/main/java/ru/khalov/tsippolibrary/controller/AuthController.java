package ru.khalov.tsippolibrary.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.khalov.tsippolibrary.dto.request.LoginRequest;
import ru.khalov.tsippolibrary.dto.request.RegisterRequest;
import ru.khalov.tsippolibrary.dto.response.AccessTokenResponse;
import ru.khalov.tsippolibrary.dto.response.AuthResponse;
import ru.khalov.tsippolibrary.service.AuthService;

/**
 * Аутентификация — ТЗ 3.2.7, ВИ-5, ВИ-5-alt
 * POST /api/auth/register — регистрация (email уникален, роль USER)
 * POST /api/auth/login — вход, выдача JWT
 * POST /api/auth/refresh — обновление по refresh cookie
 * POST /api/auth/logout — отзыв токена
 */
@Tag(name = "Authentication")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AccessTokenResponse> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletResponse response) {
        AuthResponse tokens = authService.register(request);
        setRefreshCookie(response, tokens.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AccessTokenResponse(tokens.accessToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        AuthResponse tokens = authService.login(request);
        setRefreshCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(new AccessTokenResponse(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @RequestBody(required = false) ru.khalov.tsippolibrary.dto.request.RefreshRequest body,
            HttpServletResponse response) {
        // Поддержка как cookie, так и тела запроса
        String token = refreshToken != null ? refreshToken : (body != null ? body.refreshToken() : null);
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthResponse tokens = authService.refresh(token);
        setRefreshCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(new AccessTokenResponse(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        clearRefreshToken(response);
        return ResponseEntity.ok("Logout successful");
    }

    private void clearRefreshToken(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // для локальной разработки; в проде true (требует HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 3600); // 30 дней — ТЗ 3.5.3
        response.addCookie(cookie);
    }
}
