package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class BookNotDeletedException extends ErrorResponseException {

    private static ProblemDetail asProblem(String title){
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Book not deleted");
        problemDetail.setDetail("Книга с названием: '%s', не удалена, из-за ошибки на сервере".formatted(title));
        problemDetail.setProperty("title", title);
        return problemDetail;
    }


    public BookNotDeletedException(String title) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, asProblem(title), null);
    }
}
