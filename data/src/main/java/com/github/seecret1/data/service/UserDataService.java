package com.github.seecret1.data.service;

import com.github.seecret1.commondto.model.user.UserDto;
import com.github.seecret1.data.mapper.UserMapper;
import com.github.seecret1.data.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    public List<UserDto> findAll() {
        return userMapper.toDto(userRepository.findAll());
    }

    public UserDto findById(String id) {
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found by id: " + id
                ))
        );
    }

    public UserDto createUser(UserDto userDto) {
        return userMapper.toDto(
                userRepository.save(
                        userMapper.toEntity(userDto)
                )
        );
    }
}
