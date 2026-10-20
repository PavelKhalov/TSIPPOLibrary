package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class AssignLimitExceededException extends ErrorResponseException {
    private static ProblemDetail asProblem(int limit) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Assign limit exceeded");
        pd.setDetail("Достигнут лимит активных бронирований: %d".formatted(limit));
        return pd;
    }
    public AssignLimitExceededException(int limit) { super(HttpStatus.CONFLICT, asProblem(limit), null); }
}
