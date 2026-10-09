package ru.khalov.tsippolibrary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.khalov.tsippolibrary.util.expeption.UsernameTakenException;

@RestControllerAdvice
public class GlobarExceptionHandler {

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ProblemDetail> usernameTakenException(UsernameTakenException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getBody());
    }

}
