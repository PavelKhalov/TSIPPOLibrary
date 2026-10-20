package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class NotYourAssignException extends ErrorResponseException {
    private static ProblemDetail asProblem(Long id) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Not your assign");
        pd.setDetail("Бронирование %d принадлежит другому пользователю".formatted(id));
        return pd;
    }
    public NotYourAssignException(Long id) { super(HttpStatus.FORBIDDEN, asProblem(id), null); }
}
