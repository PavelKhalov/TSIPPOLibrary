package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class BookNotUpdatedException extends ErrorResponseException {

    private static ProblemDetail asProblem(String title){
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Book not updated");
        problemDetail.setDetail("Книга: '%s' не обновлена".formatted(title));
        problemDetail.setProperty("title", title);
        return problemDetail;
    }

    public BookNotUpdatedException (String title){
        super(HttpStatus.CONFLICT, asProblem(title), null);
    }

}
