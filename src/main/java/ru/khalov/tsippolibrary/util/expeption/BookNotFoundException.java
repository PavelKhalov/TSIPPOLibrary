package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class BookNotFoundException extends ErrorResponseException {

    private static ProblemDetail asProblem(String title){
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Book not found");
        problemDetail.setDetail("Книга с названием: '%s', не найдена".formatted(title));
        problemDetail.setProperty("title", title);
        return problemDetail;
    }

    public BookNotFoundException(String title){
        super(HttpStatus.NOT_FOUND, asProblem(title), null);
    }
}
