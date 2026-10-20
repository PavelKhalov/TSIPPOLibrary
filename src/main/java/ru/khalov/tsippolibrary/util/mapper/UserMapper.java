package ru.khalov.tsippolibrary.util.mapper;

import org.springframework.stereotype.Component;
import ru.khalov.tsippolibrary.dto.UserDto;
import ru.khalov.tsippolibrary.entity.User;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRoles(),
                user.getCreatedAt()
        );
    }

    public User toUser(UserDto dto) {
        return User.builder()
                .username(dto.username())
                .email(dto.email())
                .roles(dto.roles())
                .build();
    }
}
