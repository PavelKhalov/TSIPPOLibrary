package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class NoAvailableCopiesException extends ErrorResponseException {
    private static ProblemDetail asProblem(String title) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("No available copies");
        pd.setDetail("Нет свободных экземпляров книги: '%s'".formatted(title));
        pd.setProperty("title", title);
        return pd;
    }
    public NoAvailableCopiesException(String title) {
        super(HttpStatus.CONFLICT, asProblem(title), null);
    }
}
