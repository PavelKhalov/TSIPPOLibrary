package ru.khalov.tsippolibrary.dto.request;

public record LoginRequest(
        String username,
        String password
) {
}
