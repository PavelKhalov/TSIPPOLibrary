package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class UserNotFoundException extends ErrorResponseException {
    private static ProblemDetail asProblem(Long id) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("User not found");
        pd.setDetail("Пользователь с id=%d не найден".formatted(id));
        return pd;
    }
    public UserNotFoundException(Long id) { super(HttpStatus.NOT_FOUND, asProblem(id), null); }
}
