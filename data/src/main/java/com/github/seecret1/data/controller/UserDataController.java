package com.github.seecret1.data.controller;

import com.github.seecret1.commondto.model.user.UserDto;
import com.github.seecret1.data.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserDataController {

    private final UserDataService userDataService;

    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userDataService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(userDataService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(
            @RequestBody UserDto userDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userDataService.createUser(userDto));
    }
}
