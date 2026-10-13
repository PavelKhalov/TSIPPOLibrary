package ru.khalov.tsippolibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.khalov.tsippolibrary.dto.BookDto;
import ru.khalov.tsippolibrary.entity.Book;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.repository.BookRepository;
import ru.khalov.tsippolibrary.util.expeption.BookNotDeletedException;
import ru.khalov.tsippolibrary.util.expeption.BookNotFoundException;
import ru.khalov.tsippolibrary.util.expeption.BookNotSavedException;
import ru.khalov.tsippolibrary.util.expeption.BookNotUpdatedException;
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

    @PreAuthorize("hasRole('USER') or hasAuthority('MANAGER') or hasRole('OWNER')")
    public BookDto assign(String title, User user){
        Book book = bookRepository.findByTitle(title).orElseThrow(() ->
                new BookNotFoundException(title));

        if(book.getOwner() == null && user != null){
            book.setOwner(user);
        }

        bookRepository.flush();

        return mapper.toDto(book);
    }

    @PreAuthorize("hasRole('USER') or hasAuthority('MANAGER') or hasRole('OWNER')")
    public BookDto release(String title){
        Book book = bookRepository.findByTitle(title).orElseThrow(() ->
                new BookNotFoundException(title));

        if(book.getOwner() != null){
            book.setOwner(null);
        }
        bookRepository.flush();

        return mapper.toDto(book);
    }

    @PreAuthorize("hasRole('USER') or hasAuthority('MANAGER') or hasRole('OWNER')")
    public BookDto findById(Long id){
        return mapper.toDto(bookRepository.findById(id).orElseThrow(() ->
                new BookNotFoundException("id: " + id)));
    }

    @PreAuthorize("hasRole('USER') or hasAuthority('MANAGER') or hasRole('OWNER')")
    public BookDto findByTitle(String title){
        Book book = bookRepository.findByTitle(title).orElseThrow(() ->
                new BookNotFoundException(title));

        return mapper.toDto(book);
    }


    //// Методы для менеджеров и владельца

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'OWNER')")
    public String createBook(BookDto bookDto){
        try{
            Book book = mapper.toBook(bookDto);
            bookRepository.save(book);
            bookRepository.flush();
            return "Book saved successfully";
        } catch (BookNotSavedException e){
            return e.getMessage();
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'OWNER')")
    public String editBook(String title, BookDto dto){
        Book book = bookRepository.findByTitle(title).orElseThrow( () ->
                new BookNotFoundException(title));

        if(book.getOwner() != null){
            throw new BookNotUpdatedException(title);
        }

        book.setTitle(dto.title());
        book.setAuthor(dto.author());
        book.setYearOfRelease(dto.yearOfRelease());
        book.setGenre(dto.genre());
        try {
            bookRepository.save(book);
            bookRepository.flush();
            return "Book saved successfully";
        } catch (BookNotSavedException e){
            return e.getMessage();
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'OWNER')")
    public String deleteBook(String title){
        Book book = bookRepository.findByTitle(title).orElseThrow(() ->
                    new BookNotFoundException(title));

        if(book.getOwner() != null){
            throw new BookNotDeletedException(title);
        }

        try{
            bookRepository.deleteByTitle(title);
            bookRepository.flush();

            return "Book deleted successful";
        } catch (BookNotDeletedException e){
            return e.getMessage();
        }
    }


}
