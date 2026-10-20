package ru.khalov.tsippolibrary.dto.response;

import java.util.List;

/** Статистика библиотеки — только OWNER (ТЗ 3.1.5) */
public record LibraryStatsResponse(
        long totalBooks,
        long totalUsers,
        long activeAssigns,
        List<TopBook> topBooks
) {
    public record TopBook(Long bookId, String title, long assignCount) {}
}
