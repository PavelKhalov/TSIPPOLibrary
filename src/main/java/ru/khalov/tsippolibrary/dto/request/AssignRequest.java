package ru.khalov.tsippolibrary.dto.request;

import jakarta.validation.constraints.NotNull;

/** Бронирование книги — POST /api/assign { bookId } (ТЗ 3.1.3, ВИ-3) */
public record AssignRequest(
        @NotNull Long bookId
) {
}
