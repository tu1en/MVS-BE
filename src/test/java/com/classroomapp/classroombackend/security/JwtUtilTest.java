package com.classroomapp.classroombackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set a sufficiently long secret for HS512
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "0123456789012345678901234567890123456789012345678901234567890123");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600000L);
    }

    @Test
    @DisplayName("generate/validate token and extract claims")
    void token_ok() {
        String token = jwtUtil.generateToken("user@example.com", 2);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("user@example.com", jwtUtil.getSubjectFromToken(token));
        assertEquals(2, jwtUtil.getRoleFromToken(token));
        assertEquals("TEACHER", jwtUtil.convertRoleIdToName(2));
    }

    @Test
    @DisplayName("validateToken handles invalid token")
    void token_invalid() {
        assertFalse(jwtUtil.validateToken("invalid.token.value"));
        assertNull(jwtUtil.getSubjectFromToken("invalid"));
        assertNull(jwtUtil.getRoleFromToken("invalid"));
    }
}


