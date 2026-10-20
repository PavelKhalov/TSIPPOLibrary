package ru.khalov.tsippolibrary.util.expeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class BookNotDeletedException extends ErrorResponseException {

    private static ProblemDetail asProblem(String title){
        // Исправлено: по ТЗ 3.1.2 удаление с активными бронированиями → 409 Conflict, а не 500
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Book not deleted");
        problemDetail.setDetail("Книга '%s' не может быть удалена: есть активные бронирования".formatted(title));
        problemDetail.setProperty("title", title);
        return problemDetail;
    }

    public BookNotDeletedException(String title) {
        super(HttpStatus.CONFLICT, asProblem(title), null);
    }
}
