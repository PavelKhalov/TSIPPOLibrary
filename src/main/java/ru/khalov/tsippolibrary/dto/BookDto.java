package ru.khalov.tsippolibrary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.khalov.tsippolibrary.util.Genre;

/**
 * DTO книги — соответствует диаграмме ВИ-7 и ТЗ 3.1.2.
 * В ответе передаём также id, totalCopies, availableCopies.
 */
public record BookDto(
        Long id,

        @NotBlank @Size(min = 1, max = 200) String title,

        @NotBlank @Size(min = 2, max = 200) String author,

        @NotNull Genre genre,

        @Min(value = 1000, message = "Min year is 1000")
        @Max(value = 2030, message = "Max year is 2030")
        int yearOfRelease,

        @Min(value = 1, message = "Total copies must be >= 1")
        Integer totalCopies,

        // Только для ответа — сколько доступно
        Integer availableCopies
) {
    // Упрощённый конструктор для создания (без id/available)
    public BookDto(String title, String author, int yearOfRelease, Genre genre) {
        this(null, title, author, genre, yearOfRelease, 1, null);
    }
}
