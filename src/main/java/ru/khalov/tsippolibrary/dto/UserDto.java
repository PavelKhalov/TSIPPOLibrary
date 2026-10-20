package ru.khalov.tsippolibrary.dto;

import ru.khalov.tsippolibrary.util.Role;

import java.time.Instant;
import java.util.Set;

/** DTO пользователя — без пароля (ТЗ 3.5.5: пароли не попадают в ответ) */
public record UserDto(
        Long id,
        String username,
        String email,
        Set<Role> roles,
        Instant createdAt
) {
}
