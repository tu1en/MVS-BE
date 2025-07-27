package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.UserMapper;
import com.classroomapp.classroombackend.model.enums.UserRole;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.model.usermanagement.User.RoleEnum;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<UserDto> getUsersByRole(UserRole role) {
        return userRepository.findByRoleId(role.getRoleId()).stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserDto> findAllUsers(String keyword, Pageable pageable) {
        Page<User> page = (keyword == null || keyword.isEmpty())
                ? userRepository.findAll(pageable)
                : userRepository.searchByKeyword(keyword, pageable);
        return page.map(UserMapper::toDto);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFullName(userDto.getFullName());
        user.setRoleId(userDto.getRoleId());
        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(enabled ? "active" : "inactive");
        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUserRoles(Long userId, Set<String> roleNames) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!roleNames.isEmpty()) {
            // logic map roleNames → roleId
            user.setRoleId(1);
        }
        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword("default_password");
        userRepository.save(user);
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetLink) {
        // Implement logic gửi mail
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    // =====================================================
    // PHASE 1: New methods implementation
    // =====================================================

    @Override
    public List<User> findByRoleEnum(RoleEnum roleEnum) {
        return userRepository.findByRoleEnumAndIsDeletedFalse(roleEnum);
    }

    @Override
    public List<User> findActiveUsersByRoleEnum(RoleEnum roleEnum) {
        return userRepository.findByRoleEnumAndStatusAndIsDeletedFalse(roleEnum, "active");
    }

    @Override
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeleted(true); 
        userRepository.save(user);
    }

    @Override
    public void restoreUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setDeleted(false); // ✅ sửa lại
            userRepository.save(user);
        }
    }

    @Override
    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsernameAndIsDeletedFalse(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    @Override
    public List<User> findUsersEligibleForCourseAssignment() {
        return userRepository.findUsersEligibleForCourseAssignment();
    }

    @Override
    public Map<RoleEnum, Long> getUserStatisticsByRole() {
        Map<RoleEnum, Long> statistics = new HashMap<>();
        for (RoleEnum role : RoleEnum.values()) {
            long count = userRepository.countByRoleEnumAndIsDeletedFalse(role);
            statistics.put(role, count);
        }
        return statistics;
    }

    @Override
    public boolean validateUserPermission(Long userId, String action, String resourceType, Long resourceId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return false;
        
        User user = userOpt.get();
        RoleEnum role = user.getRoleEnum();
        
        switch (role) {
            case ADMIN:
                return true;
            case MANAGER:
                return !action.equals("DELETE_USER");
            case TEACHER:
                return action.equals("VIEW") || action.equals("UPDATE_OWN_PROFILE");
            case STUDENT:
                return action.equals("VIEW_OWN_PROFILE") || action.equals("UPDATE_OWN_PROFILE");
            default:
                return false;
        }
    }

    @Override
    public Optional<User> findByUsernameActive(String username) {
        return userRepository.findByUsernameAndIsDeletedFalse(username);
    }

    @Override
    public Optional<User> findByEmailActive(String email) {
        return userRepository.findByEmailAndIsDeletedFalse(email);
    }
}
