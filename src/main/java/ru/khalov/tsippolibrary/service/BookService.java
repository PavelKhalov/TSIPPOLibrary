package ru.khalov.tsippolibrary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.khalov.tsippolibrary.dto.BookDto;
import ru.khalov.tsippolibrary.entity.Book;
import ru.khalov.tsippolibrary.repository.AssignRepository;
import ru.khalov.tsippolibrary.repository.BookRepository;
import ru.khalov.tsippolibrary.util.AssignStatus;
import ru.khalov.tsippolibrary.util.Genre;
import ru.khalov.tsippolibrary.util.expeption.BookNotDeletedException;
import ru.khalov.tsippolibrary.util.expeption.BookNotFoundException;
import ru.khalov.tsippolibrary.util.mapper.BookMapper;

/**
 * Сервис книг — ТЗ 3.1.2, диаграммы ВИ-1, ВИ-6, ВИ-7.
 * - Просмотр каталога с пагинацией/фильтрами (все авторизованные роли)
 * - Карточка книги кэшируется в Redis, инвалидируется при изменении/удалении (ТЗ 3.1.6)
 * - Создание/изменение/удаление — только ADMIN/OWNER; удаление запрещено если есть активные бронирования
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AssignRepository assignRepository;
    private final BookMapper mapper;

    // --- Чтение (все авторизованные роли) ---

    /** Список с пагинацией + фильтрами (ТЗ 3.1.2, ВИ-1) */
    public Page<BookDto> findAll(String title, String author, Genre genre, Boolean availableOnly, Pageable pageable) {
        boolean avail = Boolean.TRUE.equals(availableOnly);
        // normalize empty strings to null
        String t = (title != null && !title.isBlank()) ? title : null;
        String a = (author != null && !author.isBlank()) ? author : null;
        return bookRepository.search(t, a, genre, avail, pageable).map(mapper::toDto);
    }

    /** Карточка книги — кэшируется (ТЗ 3.1.6, ВИ-6). Fallback на БД если Redis недоступен. */
    @Cacheable(value = "books", key = "#id", unless = "#result == null")
    public BookDto findById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() ->
                new BookNotFoundException("id: " + id));
        return mapper.toDto(book);
    }

    // --- Управление (ADMIN/OWNER) ---

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @CacheEvict(value = "books", allEntries = true)
    public BookDto createBook(BookDto dto) {
        Book book = mapper.toEntityForCreate(dto);
        // available = total при создании
        if (book.getAvailableCopies() == 0) book.setAvailableCopies(book.getTotalCopies());
        Book saved = bookRepository.save(book);
        log.info("Book created: id={}, title={}", saved.getId(), saved.getTitle());
        return mapper.toDto(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @CacheEvict(value = "books", key = "#id")
    public BookDto updateBook(Long id, BookDto dto) {
        Book book = bookRepository.findById(id).orElseThrow(() ->
                new BookNotFoundException("id: " + id));

        // Пересчёт доступных экземпляров при изменении totalCopies
        int oldTotal = book.getTotalCopies();
        int oldAvailable = book.getAvailableCopies();
        int newTotal = dto.totalCopies() != null ? dto.totalCopies() : oldTotal;
        // borrowed = total - available, должен сохраниться
        int borrowed = oldTotal - oldAvailable;
        if (newTotal < borrowed) {
            throw new IllegalArgumentException("Total copies cannot be less than borrowed copies (" + borrowed + ")");
        }
        int newAvailable = newTotal - borrowed;

        book.setTitle(dto.title());
        book.setAuthor(dto.author());
        book.setGenre(dto.genre());
        book.setYearOfRelease(dto.yearOfRelease());
        book.setTotalCopies(newTotal);
        book.setAvailableCopies(newAvailable);

        Book saved = bookRepository.save(book);
        return mapper.toDto(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() ->
                new BookNotFoundException("id: " + id));

        // ТЗ 3.1.2: удаление книги с активными бронированиями запрещается → 409
        // Исправлено: считаем через exists/count, а не через Page.getTotalElements()
        boolean hasActive = assignRepository.existsByBookIdAndStatus(id, AssignStatus.ACTIVE);
        if (hasActive) {
            throw new BookNotDeletedException(book.getTitle());
        }

        bookRepository.delete(book);
        log.info("Book deleted: id={}, title={}", id, book.getTitle());
    }
}
