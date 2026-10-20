package ru.khalov.tsippolibrary.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос регистрации — ТЗ 3.1.1: имя, email (уникален), пароль.
 * Новому пользователю автоматически присваивается роль USER.
 */
public record RegisterRequest(
        @NotBlank @Size(min = 1, max = 100) String username,

        @NotBlank @Email @Size(max = 255) String email,

        @NotBlank @Size(min = 6, max = 100) String password
) {
}
