package ru.khalov.tsippolibrary.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.khalov.tsippolibrary.dto.BookDto;
import ru.khalov.tsippolibrary.service.BookService;
import ru.khalov.tsippolibrary.util.Genre;

/**
 * Книги — ТЗ 3.2.7, ВИ-1, ВИ-6, ВИ-7
 * GET    /api/books        — список с пагинацией/фильтрами (все роли)
 * GET    /api/books/{id}   — карточка (кэш Redis)
 * POST   /api/books        — создать (ADMIN/OWNER)
 * PUT    /api/books/{id}   — изменить (ADMIN/OWNER)
 * DELETE /api/books/{id}   — удалить (ADMIN/OWNER, нельзя если есть активные бронирования)
 */
@Tag(name = "Books")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<Page<BookDto>> findAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) Boolean availableOnly,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(bookService.findAll(title, author, genre, availableOnly, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookDto> create(@RequestBody @Valid BookDto dto) {
        BookDto created = bookService.createBook(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDto> update(@PathVariable Long id, @RequestBody @Valid BookDto dto) {
        return ResponseEntity.ok(bookService.updateBook(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
