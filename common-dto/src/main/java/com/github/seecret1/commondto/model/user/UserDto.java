package com.github.seecret1.commondto.model.user;

import jakarta.validation.constraints.NotBlank;

public record UserDto(

        String id,

        @NotBlank(message = "name must be set!")
        String name
) { }
