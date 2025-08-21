package com.team2.university_room_booking.controller;

import com.team2.university_room_booking.dto.request.LoginDto;
import com.team2.university_room_booking.dto.request.RegisterUserDto;
import com.team2.university_room_booking.dto.response.JwtResponse;
import com.team2.university_room_booking.dto.response.UserDto;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.User;
import com.team2.university_room_booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final DtoMapper dtoMapper;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterUserDto userDto) {
        return ResponseEntity.ok(userService.registerUser(userDto));
    }


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginDto loginRequest) {
        String token = userService.login(loginRequest);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dtoMapper.toUserDto(user));
    }
}
