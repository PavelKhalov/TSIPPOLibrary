package ru.khalov.tsippolibrary.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.khalov.tsippolibrary.dto.BookDto;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.service.BookService;

import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;

    @GetMapping()
    public ResponseEntity<List<BookDto>> findAllBook(@RequestParam(value = "page", required = false) Integer page,
                                                     @RequestParam(value = "itemsPerPage", required = false) Integer itemsPerPage,
                                                     @RequestParam(value = "sortByYear", required = false) boolean sort,
                                                     @RequestParam(value = "title", required = false) String title) {

        if (page != null && itemsPerPage != null && page >= 0 && itemsPerPage >= 0) {
            return ResponseEntity.ok().body(bookService.findBookWithPagination(page, itemsPerPage));
        } else if (sort){
            return ResponseEntity.ok().body(bookService.findBookWithSort());
        } else if(title != null) {
            return ResponseEntity.ok().body(List.of(bookService.findByTitle(title)));
        }
        else{
            return ResponseEntity.ok().body(bookService.findAllBook());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> findById(@PathVariable Long id){
        return ResponseEntity.ok().body(bookService.findById(id));
    }

    @PostMapping("/assign")
    public ResponseEntity<BookDto> assignBook(@RequestParam(value = "title") String title,
                                              @AuthenticationPrincipal User user){
        return ResponseEntity.ok().body(bookService.assign(title, user));
    }

    @PostMapping("/release")
    public ResponseEntity<BookDto> releaseBook(@RequestParam(value = "title") String title){
        return ResponseEntity.ok().body(bookService.release(title));
    }


    //// Методы менеджеров и владельцев
    // TODO

    @PostMapping()
    public ResponseEntity<String> createBook(@RequestBody @Valid BookDto bookDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(bookDto));
    }

    @PostMapping("/update")
    public ResponseEntity<String> editBook(@RequestParam(value = "title") String title, @RequestBody @Valid BookDto bookDto){
        return ResponseEntity.ok().body(bookService.editBook(title, bookDto));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteBook(@RequestParam(value = "title") String title){
        return ResponseEntity.ok().body(bookService.deleteBook(title));
    }

}
