package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class AlreadyAssignedException extends ErrorResponseException {
    private static ProblemDetail asProblem(String title) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Already assigned");
        pd.setDetail("Книга '%s' уже забронирована вами".formatted(title));
        return pd;
    }
    public AlreadyAssignedException(String title) { super(HttpStatus.CONFLICT, asProblem(title), null); }
}
