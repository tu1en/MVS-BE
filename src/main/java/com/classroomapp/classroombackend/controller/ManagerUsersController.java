package com.classroomapp.classroombackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.usermanagement.UserDTO;
import com.classroomapp.classroombackend.model.enums.UserRole;
import com.classroomapp.classroombackend.service.usermanagement.UserService;

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
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {
        log.info("Manager requesting all users with keyword: {}", keyword);
        Page<UserDTO> users = userService.findAllUsers(keyword, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        log.info("Manager requesting user details for ID: {}", userId);
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
            @PathVariable Integer roleId,
            @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {
        log.info("Manager requesting users by role: {}", roleId);
        List<UserDTO> users = userService.getUsersByRole(convertRoleIdToEnum(roleId));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());
        List<UserDTO> pageContent = users.subList(start, end);

        Page<UserDTO> page = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, users.size());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        log.info("Manager requesting user statistics");

        List<UserDTO> allUsers = userService.getAllUsers();
        long totalUsers = allUsers.size();
        long students = allUsers.stream().filter(u -> u.getRoleEnum() == UserRole.STUDENT).count();
        long teachers = allUsers.stream().filter(u -> u.getRoleEnum() == UserRole.TEACHER).count();
        long managers = allUsers.stream().filter(u -> u.getRoleEnum() == UserRole.MANAGER).count();
        long admins = allUsers.stream().filter(u -> u.getRoleEnum() == UserRole.ADMIN).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("students", students);
        stats.put("teachers", teachers);
        stats.put("managers", managers);
        stats.put("admins", admins);

        return ResponseEntity.ok(stats);
    }

    private UserRole convertRoleIdToEnum(Integer roleId) {
        if (roleId == null) return UserRole.STUDENT;
        switch (roleId) {
            case 1: return UserRole.STUDENT;
            case 2: return UserRole.TEACHER;
            case 3: return UserRole.MANAGER;
            case 4: return UserRole.ADMIN;
            case 5: return UserRole.ACCOUNTANT;
            default: return UserRole.STUDENT;
        }
    }
}
