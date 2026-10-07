package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class RefreshTokenException extends ErrorResponseException {

    public static ProblemDetail asProblem(String detail){
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setDetail(detail);
        problemDetail.setTitle("Refresh token");
        return problemDetail;
    }

    public RefreshTokenException(String detail){
        super(HttpStatus.CONFLICT, asProblem(detail), null);
    }
}
