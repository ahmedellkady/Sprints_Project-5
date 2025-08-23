package com.team2.university_room_booking.service;

import com.team2.university_room_booking.security.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;


    private String token;
    private final String USERNAME = "testuser";
    private final String ROLE = "STUDENT";



    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        Field expirationField = JwtUtil.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtUtil, 1000 * 60 * 60); // 1 hour
        token = jwtUtil.generateToken(USERNAME, ROLE);
    }

    @Test
    void testGenerateToken() {
        assertNotNull(token);
    }

    @Test
    void testExtractUsername_ReturnCorrectUsername() {
        String username = jwtUtil.extractUsername(token);
        assertEquals(USERNAME, username);
    }

    @Test
    void testExtractRole_ReturnCorrectRole() {
        String role = jwtUtil.extractRole(token);
        assertEquals(ROLE, role);
    }

    @Test
    void testInvalidToken_ThrowJwtException() {
        String invalidToken = "invalid.token.here";

        assertThrows(JwtException.class, () -> jwtUtil.extractUsername(invalidToken));
        assertThrows(JwtException.class, () -> jwtUtil.extractRole(invalidToken));
    }
}
