package com.classroomapp.classroombackend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/manager/users")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
@Slf4j
public class ManagerUsersController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {
        log.info("Manager requesting all users with keyword: {}", keyword);
        Page<UserDto> users = userService.findAllUsers(keyword, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        log.info("Manager requesting user details for ID: {}", userId);
        UserDto user = userService.FindUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<Page<UserDto>> getUsersByRole(
            @PathVariable Integer roleId,
            @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {
        log.info("Manager requesting users by role: {}", roleId);
        // For now, using existing method - should be enhanced to support pagination
        java.util.List<UserDto> users = userService.FindUsersByRole(roleId);
        
        // Convert List to Page manually (simple implementation)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());
        java.util.List<UserDto> pageContent = users.subList(start, end);
        
        Page<UserDto> page = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, users.size());
        
        return ResponseEntity.ok(page);
    }

    @GetMapping("/statistics")
    public ResponseEntity<java.util.Map<String, Object>> getUserStatistics() {
        log.info("Manager requesting user statistics");
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // Get counts by role
        java.util.List<UserDto> allUsers = userService.FindAllUsers();
        long totalUsers = allUsers.size();
        long students = allUsers.stream().filter(u -> u.getRoleId() == 1).count();
        long teachers = allUsers.stream().filter(u -> u.getRoleId() == 2).count();
        long managers = allUsers.stream().filter(u -> u.getRoleId() == 3).count();
        long admins = allUsers.stream().filter(u -> u.getRoleId() == 0).count();
        
        stats.put("totalUsers", totalUsers);
        stats.put("students", students);
        stats.put("teachers", teachers);
        stats.put("managers", managers);
        stats.put("admins", admins);
        
        return ResponseEntity.ok(stats);
    }
} 