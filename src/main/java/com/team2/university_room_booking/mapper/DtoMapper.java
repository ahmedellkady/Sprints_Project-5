package com.team2.university_room_booking.mapper;

import com.team2.university_room_booking.dto.request.RegisterUserDto;
import com.team2.university_room_booking.dto.response.UserDto;
import com.team2.university_room_booking.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    private final ModelMapper modelMapper;

    public DtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public User toUserEntity(RegisterUserDto dto) {
        return modelMapper.map(dto, User.class);
    }

    public UserDto toUserDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }
}