package com.team2.university_room_booking.dto.response;

import com.team2.university_room_booking.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    private String username;

    private String email;

    private Role role;

    // password is not included in the DTO for security reasons
}