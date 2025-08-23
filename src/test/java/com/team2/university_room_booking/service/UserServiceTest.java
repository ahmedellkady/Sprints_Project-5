package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.LoginDto;
import com.team2.university_room_booking.dto.request.RegisterUserDto;
import com.team2.university_room_booking.dto.response.UserDto;
import com.team2.university_room_booking.enums.Role;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.exceptions.UserAlreadyExistsException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.User;
import com.team2.university_room_booking.repository.UserRepository;
import com.team2.university_room_booking.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private DtoMapper dtoMapper;

    @InjectMocks
    private UserService userService;

    private final String username = "testuser";

    private final RegisterUserDto registerDto = new RegisterUserDto(username, "test@test.com", "password", Role.STUDENT);
    private final LoginDto loginDto = new LoginDto(username, "password");
    private final UserDto userDto = new UserDto(1L, username, "test@test.com", Role.STUDENT);

    @Test
    void registerUser_Success() {
        User userEntity = new User();
        userEntity.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(dtoMapper.toUserEntity(registerDto)).thenReturn(userEntity);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(dtoMapper.toUserDto(userEntity)).thenReturn(userDto);

        UserDto result = userService.registerUser(registerDto);

        assertEquals(username, result.getUsername());

        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_ThrowAlreadyExistsException() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(registerDto));
    }

    @Test
    void login_Success() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));
        when(dtoMapper.toUserDto(any(User.class))).thenReturn(userDto);
        when(jwtUtil.generateToken(username, "STUDENT")).thenReturn("jwt-token");

        String token = userService.login(loginDto);

        assertEquals("jwt-token", token);
    }

    @Test
    void login_ThrowResourceNotFoundException() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        when(authenticationManager.authenticate(any())).thenReturn(auth);

        assertThrows(ResourceNotFoundException.class, () -> userService.login(loginDto));
    }
}
