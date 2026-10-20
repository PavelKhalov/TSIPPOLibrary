package ru.khalov.tsippolibrary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.khalov.tsippolibrary.dto.response.AssignResponse;
import ru.khalov.tsippolibrary.entity.Assign;
import ru.khalov.tsippolibrary.entity.Book;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.repository.AssignRepository;
import ru.khalov.tsippolibrary.repository.BookRepository;
import ru.khalov.tsippolibrary.util.AssignStatus;
import ru.khalov.tsippolibrary.util.expeption.BookNotFoundException;
import ru.khalov.tsippolibrary.util.mapper.AssignMapper;

import java.time.Instant;

/**
 * Бронирование и возврат книг — ТЗ 3.1.3, диаграммы ВИ-3, ВИ-3-alt, ВИ-4.
 * - Проверка наличия свободных экземпляров (409 если нет)
 * - Запрет повторного бронирования, лимит 5 активных
 * - Пессимистическая блокировка книги для защиты от гонки
 * - Возврат — только своё активное бронирование, без подтверждения
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignService {

    private final AssignRepository assignRepository;
    private final BookRepository bookRepository;
    private final AssignMapper assignMapper;

    private static final int MAX_ACTIVE = 5;

    @Transactional
    @CacheEvict(value = "books", key = "#bookId")
    public AssignResponse assign(Long bookId, User currentUser) {
        // Пессимистическая блокировка строки книги
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BookNotFoundException("id: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new ru.khalov.tsippolibrary.util.expeption.NoAvailableCopiesException(book.getTitle());
        }
        if (assignRepository.existsByUserIdAndBookIdAndStatus(currentUser.getId(), bookId, AssignStatus.ACTIVE)) {
            throw new ru.khalov.tsippolibrary.util.expeption.AlreadyAssignedException(book.getTitle());
        }
        long activeCount = assignRepository.countByUserIdAndStatus(currentUser.getId(), AssignStatus.ACTIVE);
        if (activeCount >= MAX_ACTIVE) {
            throw new ru.khalov.tsippolibrary.util.expeption.AssignLimitExceededException(MAX_ACTIVE);
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Assign assign = Assign.builder()
                .user(currentUser)
                .book(book)
                .status(AssignStatus.ACTIVE)
                .build();
        Assign saved = assignRepository.save(assign);
        log.info("Assign created: user={}, book={}, remaining={}", currentUser.getId(), bookId, book.getAvailableCopies());
        return assignMapper.toDto(saved);
    }

    @Transactional
    @CacheEvict(value = "books", key = "#result.bookId()")
    public AssignResponse release(Long assignId, User currentUser) {
        Assign assign = assignRepository.findById(assignId)
                .orElseThrow(() -> new ru.khalov.tsippolibrary.util.expeption.AssignNotFoundException(assignId));

        if (!assign.getUser().getId().equals(currentUser.getId())) {
            throw new ru.khalov.tsippolibrary.util.expeption.NotYourAssignException(assignId);
        }
        if (assign.getStatus() != AssignStatus.ACTIVE) {
            throw new IllegalStateException("Assign already returned");
        }

        // Блокируем книгу для корректного увеличения available
        Book book = bookRepository.findByIdForUpdate(assign.getBook().getId())
                .orElseThrow(() -> new BookNotFoundException("id: " + assign.getBook().getId()));

        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            log.warn("Available copies already at max for book {}", book.getId());
        } else {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        }

        assign.setStatus(AssignStatus.RETURNED);
        assign.setReturnedAt(Instant.now());
        Assign saved = assignRepository.save(assign);
        log.info("Assign returned: id={}, user={}, book={}", assignId, currentUser.getId(), book.getId());
        return assignMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<AssignResponse> myAssigns(Long userId, Pageable pageable) {
        return assignRepository.findByUserId(userId, pageable).map(assignMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AssignResponse> allAssigns(Long userId, Long bookId, AssignStatus status, Pageable pageable) {
        return assignRepository.searchAll(userId, bookId, status, pageable).map(assignMapper::toDto);
    }
}
