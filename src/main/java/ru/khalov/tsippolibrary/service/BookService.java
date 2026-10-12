package ru.khalov.tsippolibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.khalov.tsippolibrary.dto.BookDto;
import ru.khalov.tsippolibrary.entity.Book;
import ru.khalov.tsippolibrary.repository.BookRepository;
import ru.khalov.tsippolibrary.util.expeption.BookNotDeletedException;
import ru.khalov.tsippolibrary.util.expeption.BookNotFoundException;
import ru.khalov.tsippolibrary.util.mapper.BookMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper mapper;

    //// Методы для пользователей

    @PreAuthorize("hasRole('USER') or hasAuthority('MANAGER') or hasRole('OWNER')")
    public List<BookDto> findAllBook(){
        return bookRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @PreAuthorize("hasRole('USER') or hasAuthority('MANAGER') or hasRole('OWNER')")
    public List<BookDto> findBookWithPagination(Integer page, Integer itemsPerPage) {
        return bookRepository.findAll(PageRequest.of(page, itemsPerPage)).stream().map(mapper::toDto).toList();
    }

    @PreAuthorize("hasRole('USER') or hasAuthority('MANAGER') or hasRole('OWNER')")
    public List<BookDto> findBookWithSort(){
        return bookRepository.findAll(Sort.by("yearOfRelease")).stream().map(mapper::toDto).toList();
    }

    //// Методы для менеджеров и владельца

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'OWNER')")
    public Book createBook(BookDto bookDto){

        return bookRepository.save(mapper.toBook(bookDto));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'OWNER')")
    public Book editBook(String title, BookDto dto){
        Book book = bookRepository.findByTitle(title).orElseThrow( () ->
                new BookNotFoundException(title));

        book.setTitle(dto.title());
        book.setAuthor(dto.author());
        book.setYearOfRelease(dto.yearOfRelease());
        book.setGenre(dto.genre());

        bookRepository.save(book);

        return book;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'OWNER')")
    public String deleteBook(String title){
        try {
            bookRepository.deleteByTitle(title);
            bookRepository.flush();

            return "Book deleted successful";
        } catch (BookNotDeletedException e){
            return e.getMessage();
        }
    }


}
