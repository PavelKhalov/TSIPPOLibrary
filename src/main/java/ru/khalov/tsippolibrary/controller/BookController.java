package ru.khalov.tsippolibrary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.khalov.tsippolibrary.dto.BookDto;
import ru.khalov.tsippolibrary.service.BookService;

import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookDto>> findAllBook(@RequestParam(value = "page", required = false) Integer page,
                                                     @RequestParam(value = "itemsPerPage", required = false) Integer itemsPerPage,
                                                     @RequestParam(value = "sortByYear", required = false) boolean sort){

        if (page != null && itemsPerPage != null && page >= 0 && itemsPerPage >= 0) {
            return ResponseEntity.ok().body(bookService.findBookWithPagination(page, itemsPerPage));
        } else if (sort){
            return ResponseEntity.ok().body(bookService.findBookWithSort());
        } else{
            return ResponseEntity.ok().body(bookService.findAllBook());
        }

    }


}
