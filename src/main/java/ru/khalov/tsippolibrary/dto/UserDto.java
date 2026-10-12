package ru.khalov.tsippolibrary.dto;

import jakarta.validation.constraints.Size;

public record UserDto(

        @Size(min = 1, max = 100, message = "Username should be from 1 to 100")
        String username

) {
}
