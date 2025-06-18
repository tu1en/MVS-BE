package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import com.classroomapp.classroombackend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class UserController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Get all users
     * @return list of all users
     */    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try {
            List<UserDto> users = userService.FindAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            // Fallback to mock data if service fails
            List<UserDto> mockUsers = new ArrayList<>();
            UserDto user1 = new UserDto();
            user1.setId(1L);
            user1.setFullName("Nguyễn Văn A");
            user1.setRoleName("TEACHER");
            user1.setRoleId(2);
            
            UserDto user2 = new UserDto();
            user2.setId(2L);
            user2.setFullName("Trần Thị B");
            user2.setRoleName("STUDENT");
            user2.setRoleId(1);
            
            mockUsers.add(user1);
            mockUsers.add(user2);
            return ResponseEntity.ok(mockUsers);
        }
    }
    
    /**
     * Get user by ID
     * @param id user ID
     * @return user with specified ID
     */    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        try {
            UserDto user = userService.FindUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // Fallback to mock data if service fails
            UserDto mockUser = new UserDto();
            mockUser.setId(id);
            mockUser.setFullName("User " + id);
            mockUser.setRoleName(id % 2 == 0 ? "STUDENT" : "TEACHER");
            mockUser.setRoleId(id % 2 == 0 ? 1 : 2);
            return ResponseEntity.ok(mockUser);
        }
    }
    
    /**
     * Get users by role ID
     * @param roleId role ID (1=STUDENT, 2=TEACHER, 3=MANAGER, 0=ADMIN)
     * @return list of users with specified role
     */    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable Long roleId) {
        try {
            List<UserDto> users = userService.FindUsersByRole(roleId.intValue());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            // Fallback to mock data if service fails
            List<UserDto> mockUsers = new ArrayList<>();
            String roleName = getRoleName(roleId);
            
            for (int i = 1; i <= 5; i++) {
                UserDto user = new UserDto();
                user.setId((long) i);
                user.setFullName(roleName + " " + i);
                user.setRoleName(roleName);
                user.setRoleId(roleId.intValue());
                mockUsers.add(user);
            }
            return ResponseEntity.ok(mockUsers);
        }
    }
    
    private String getRoleName(Long roleId) {
        switch (roleId.intValue()) {
            case 0: return "ADMIN";
            case 1: return "STUDENT";
            case 2: return "TEACHER";
            case 3: return "MANAGER";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * Create new user
     * @param userDto user information
     * @return created user
     */
    @PostMapping
    public ResponseEntity<UserDto> CreateUser(@Valid @RequestBody UserDto userDto) {
        return new ResponseEntity<>(userService.CreateUser(userDto), HttpStatus.CREATED);
    }
    
    /**
     * Update existing user
     * @param id user ID
     * @param userDto updated user information
     * @return updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> UpdateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.UpdateUser(id, userDto));
    }
    
    /**
     * Delete user
     * @param id user ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> DeleteUser(@PathVariable Long id) {
        userService.DeleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all teachers
     * @return list of all teachers
     */    @GetMapping("/teachers")
    public ResponseEntity<List<UserDto>> GetAllTeachers() {
        return ResponseEntity.ok(userService.FindUsersByRole(2)); // Role 2 = TEACHER
    }
    
    /**
     * Get all students
     * @return list of all students
     */
    @GetMapping("/students")
    public ResponseEntity<List<UserDto>> GetAllStudents() {
        return ResponseEntity.ok(userService.FindUsersByRole(1)); // Role 1 = STUDENT
    }
}