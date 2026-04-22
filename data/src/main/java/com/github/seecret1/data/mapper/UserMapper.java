package com.github.seecret1.data.mapper;

import com.github.seecret1.commondto.model.user.UserDto;
import com.github.seecret1.data.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    User toEntity(UserDto dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    UserDto toDto(User user);

    List<UserDto> toDto(List<User> users);
}
