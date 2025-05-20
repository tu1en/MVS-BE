package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Get all users
     * @return list of all users
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> GetAllUsers() {
        return ResponseEntity.ok(userService.FindAllUsers());
    }
    
    /**
     * Get user by ID
     * @param id user ID
     * @return user with specified ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> GetUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.FindUserById(id));
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
} 