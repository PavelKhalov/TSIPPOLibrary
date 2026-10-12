package ru.khalov.tsippolibrary.util.mapper;

import org.springframework.stereotype.Component;
import ru.khalov.tsippolibrary.dto.UserDto;
import ru.khalov.tsippolibrary.entity.User;

@Component
public class UserMapper {

    public UserDto toDto(User user){
        return new UserDto(user.getUsername());
    }

    public User toUser(UserDto userDto){
        return User.builder().username(userDto.username()).build();
    }

}
