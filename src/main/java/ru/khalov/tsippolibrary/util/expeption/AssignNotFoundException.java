package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class AssignNotFoundException extends ErrorResponseException {
    private static ProblemDetail asProblem(Long id) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Assign not found");
        pd.setDetail("Бронирование с id=%d не найдено".formatted(id));
        return pd;
    }
    public AssignNotFoundException(Long id) { super(HttpStatus.NOT_FOUND, asProblem(id), null); }
}
