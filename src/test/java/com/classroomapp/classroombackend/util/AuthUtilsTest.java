package com.classroomapp.classroombackend.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthUtilsTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentUsername and userId derive from authentication")
    void currentUser_ok() {
        var auth = new TestingAuthenticationToken("alice", "pwd",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertEquals("alice", AuthUtils.getCurrentUsername());
        assertNotNull(AuthUtils.getCurrentUserId());
    }

    @Test
    @DisplayName("hasRole and hasAnyRole reflect authorities")
    void roles_ok() {
        var auth = new TestingAuthenticationToken("bob", "pwd",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_MANAGER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertTrue(AuthUtils.hasRole("ROLE_ADMIN"));
        assertTrue(AuthUtils.hasAnyRole("ROLE_TEACHER", "ROLE_MANAGER"));
        assertFalse(AuthUtils.hasRole("ROLE_STUDENT"));
    }
}


