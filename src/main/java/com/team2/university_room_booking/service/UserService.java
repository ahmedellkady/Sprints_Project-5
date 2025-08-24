package com.team2.university_room_booking.service;

import com.team2.university_room_booking.dto.request.LoginDto;
import com.team2.university_room_booking.dto.request.RegisterUserDto;
import com.team2.university_room_booking.dto.response.UserDto;
import com.team2.university_room_booking.exceptions.InvalidLoginException;
import com.team2.university_room_booking.exceptions.ResourceNotFoundException;
import com.team2.university_room_booking.exceptions.UserAlreadyExistsException;
import com.team2.university_room_booking.mapper.DtoMapper;
import com.team2.university_room_booking.model.User;
import com.team2.university_room_booking.repository.UserRepository;
import com.team2.university_room_booking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final DtoMapper dtoMapper;

    //Register a new user
    public UserDto registerUser(RegisterUserDto userDto) {
        // Check if username already exists
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User with this username already exists");
        }
        User user = dtoMapper.toUserEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Save user
        User savedUser = userRepository.save(user);
        log.info("Registering new user");
        return dtoMapper.toUserDto(savedUser);
    }

    // Login and return JWT token
    public String login(LoginDto loginDto) {
        try {
            // Try authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new InvalidLoginException("Invalid username or password");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT
            UserDto user = findByUsername(loginDto.getUsername());
            return jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        } catch (Exception ex) {
            throw new ResourceNotFoundException("Invalid username or password");
        }
    }

    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return dtoMapper.toUserDto(user);
    }
}