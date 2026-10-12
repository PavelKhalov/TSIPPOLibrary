package ru.khalov.tsippolibrary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.khalov.tsippolibrary.util.Genre;

public record BookDto(

        @Size(min = 1, max = 100, message = "title should be from 1 to 100")
        String title,

        @Size(min = 6, max = 100, message = "fio author should be from 6 to 100, example: А.С.Пушкин")
        @Pattern(regexp = "[А-ЯЁ].[А-ЯЁ]. [А-ЯЁ][а-яё]+")
        String author,

        @Min(value = 1500, message = "Min year of release is 1500")
        @Max(value = 2026, message = "Max year of release is 2026")
        int yearOfRelease,

        Genre genre
) {
}
