package ru.khalov.tsippolibrary.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Редактирование профиля: имя/email (ТЗ 3.1.4) */
public record UserUpdateRequest(
        @Size(min = 1, max = 100) String username,
        @Email @Size(max = 255) String email
) {
}
