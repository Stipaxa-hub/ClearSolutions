package org.task.clearsolutions.mapper;

import org.mapstruct.Mapper;
import org.task.clearsolutions.config.MapperConfig;
import org.task.clearsolutions.dto.UserRequestDto;
import org.task.clearsolutions.dto.UserResponseDto;
import org.task.clearsolutions.entity.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {

    UserResponseDto toDto(User user);

    User toEntity(UserRequestDto userRequestDto);

}
