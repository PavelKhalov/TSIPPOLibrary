package ru.khalov.tsippolibrary.dto.response;

import ru.khalov.tsippolibrary.util.AssignStatus;

import java.time.Instant;

/** Ответ по бронированию — для ВИ-3, ВИ-3-alt, ВИ-4 */
public record AssignResponse(
        Long id,
        Long userId,
        String username,
        Long bookId,
        String bookTitle,
        AssignStatus status,
        Instant createdAt,
        Instant returnedAt
) {
}
