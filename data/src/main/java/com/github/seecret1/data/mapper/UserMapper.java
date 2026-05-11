package com.github.seecret1.data.mapper;

import com.github.seecret1.commondto.model.user.UserDto;
import com.github.seecret1.data.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {

    public User toEntity(UserDto dto) {
        return User.builder()
                .name(dto.name())
                .id(dto.id())
                .build();
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName());
    }

    public List<UserDto> toDto(List<User> users) {
        List<UserDto> list = new ArrayList<>(users.size());
        for (User user : users) {
            list.add(new UserDto(user.getId(), user.getName()));
        }
        return list;
    }
}
