package com.classroomapp.classroombackend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.usermanagement.UpdateUserRolesRequest;
import com.classroomapp.classroombackend.dto.usermanagement.UpdateUserStatusRequest;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {
        Page<UserDto> users = userService.findAllUsers(keyword, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ Admin mới được đổi status
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest statusRequest) {
        UserDto updatedUser = userService.updateUserStatus(userId, statusRequest.getEnabled());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest rolesRequest) {
        try {
            UserDto updatedUser = userService.updateUserRoles(userId, rolesRequest.getRoles());
            return ResponseEntity.ok(updatedUser);
        } catch (BusinessLogicException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
} 