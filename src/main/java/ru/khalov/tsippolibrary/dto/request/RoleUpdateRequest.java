package ru.khalov.tsippolibrary.dto.request;

import jakarta.validation.constraints.NotNull;
import ru.khalov.tsippolibrary.util.Role;

/** Запрос на назначение/снятие роли ADMIN — только OWNER (ТЗ 3.1.5, PATCH /api/users/{id}/role) */
public record RoleUpdateRequest(
        @NotNull Role role
) {
}
