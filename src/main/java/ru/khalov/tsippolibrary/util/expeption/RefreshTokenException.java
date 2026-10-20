package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class RefreshTokenException extends ErrorResponseException {

    public static ProblemDetail asProblem(String detail){
        // Исправлено: по ТЗ 3.5.3 истёкший/отозванный refresh → 401 (требуется повторный вход), а не 409
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setDetail(detail);
        problemDetail.setTitle("Refresh token");
        return problemDetail;
    }

    public RefreshTokenException(String detail){
        super(HttpStatus.UNAUTHORIZED, asProblem(detail), null);
    }
}
