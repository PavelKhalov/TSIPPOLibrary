package ru.khalov.tsippolibrary.util.mapper;

import org.springframework.stereotype.Component;
import ru.khalov.tsippolibrary.dto.BookDto;
import ru.khalov.tsippolibrary.entity.Book;

@Component
public class BookMapper {

    public Book toBook(BookDto dto){
        return Book.builder()
                .title(dto.title())
                .author(dto.author())
                .yearOfRelease(dto.yearOfRelease())
                .genre(dto.genre())
                .build();
    }

    public BookDto toDto(Book book){
        return new BookDto(book.getTitle(), book.getAuthor(), book.getYearOfRelease(), book.getGenre());
    }
}
