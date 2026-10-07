package ru.khalov.tsippolibrary.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
