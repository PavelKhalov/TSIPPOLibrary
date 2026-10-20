package ru.khalov.tsippolibrary.util.mapper;

import org.springframework.stereotype.Component;
import ru.khalov.tsippolibrary.dto.BookDto;
import ru.khalov.tsippolibrary.entity.Book;

@Component
public class BookMapper {

    public Book toBook(BookDto dto) {
        return Book.builder()
                .title(dto.title())
                .author(dto.author())
                .yearOfRelease(dto.yearOfRelease())
                .genre(dto.genre())
                .totalCopies(dto.totalCopies() != null ? dto.totalCopies() : 1)
                .availableCopies(dto.availableCopies() != null ? dto.availableCopies()
                        : (dto.totalCopies() != null ? dto.totalCopies() : 1))
                .build();
    }

    /** Создаёт сущность без id (для save) */
    public Book toEntityForCreate(BookDto dto) {
        Book b = toBook(dto);
        b.setId(null);
        if (b.getAvailableCopies() == 0) b.setAvailableCopies(b.getTotalCopies());
        return b;
    }

    public BookDto toDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getYearOfRelease(),
                book.getTotalCopies(),
                book.getAvailableCopies()
        );
    }
}
