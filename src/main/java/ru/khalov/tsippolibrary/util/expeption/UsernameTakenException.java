package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.ErrorResponseException;

public class UsernameTakenException extends ErrorResponseException {

    private static ProblemDetail asProblem(String username){
        ProblemDetail problemDetail =ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Username taken");
        problemDetail.setDetail("Пользователь с именем: '%s' уже существует".formatted(username));
        problemDetail.setProperty("username", username);
        return problemDetail;
    }

    public UsernameTakenException(String username){
        super(HttpStatus.CONFLICT, asProblem(username), null);
    }
}
