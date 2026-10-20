package ru.khalov.tsippolibrary.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Вход по email + пароль, выдача JWT (ТЗ 3.1.1) */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
