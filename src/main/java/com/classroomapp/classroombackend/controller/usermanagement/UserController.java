package com.classroomapp.classroombackend.controller.usermanagement;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.usermanagement.UserDTO;
import com.classroomapp.classroombackend.dto.usermanagement.UserMapper;
import com.classroomapp.classroombackend.model.enums.UserRole;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.service.usermanagement.UserService;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;

    // ✅ 1. Lấy tất cả user (có phân trang)
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sortBy) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
            List<UserDTO> users = userService.getAllUsers(); // Service trả về DTO
            Page<UserDTO> pageResult = new PageImpl<>(users, pageable, users.size());
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ 2. Lấy user hiện tại (dựa trên Security Context)
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else {
                email = principal.toString();
            }

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            return ResponseEntity.ok(UserMapper.toDto(user));
        } catch (Exception e) {
            logger.severe("Error retrieving current user profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ 3. Lấy chi tiết user theo ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ 4. Lấy user theo role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable UserRole role) {
        try {
            List<UserDTO> users = userService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/managers")
    public ResponseEntity<List<UserDTO>> getAllManagers() {
        return getUsersByRole(UserRole.MANAGER);
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<UserDTO>> getAllTeachers() {
        return getUsersByRole(UserRole.TEACHER);
    }

    @GetMapping("/students")
    public ResponseEntity<List<UserDTO>> getAllStudents() {
        return getUsersByRole(UserRole.STUDENT);
    }

    // ✅ 5. Lấy danh sách department (unique)
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        try {
            List<String> departments = userService.getAllUsers().stream()
                    .map(UserDTO::getDepartment)
                    .filter(dept -> dept != null && !dept.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    // ✅ 6. Tìm kiếm user theo tên/department/role
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) UserRole role) {
        try {
            List<UserDTO> users = userService.getAllUsers();

            if (name != null && !name.trim().isEmpty()) {
                users = users.stream()
                        .filter(user -> user.getFullName() != null &&
                                user.getFullName().toLowerCase().contains(name.toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (department != null && !department.trim().isEmpty()) {
                users = users.stream()
                        .filter(user -> user.getDepartment() != null &&
                                user.getDepartment().equalsIgnoreCase(department))
                        .collect(Collectors.toList());
            }

            if (role != null) {
                users = users.stream()
                        .filter(user -> user.getRoleEnum() == role)
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    // ✅ 7. Đếm user theo role
    @GetMapping("/count/by-role")
    public ResponseEntity<Map<UserRole, Long>> countUsersByRole() {
        try {
            Map<UserRole, Long> counts = Arrays.stream(UserRole.values())
                    .collect(Collectors.toMap(
                            role -> role,
                            role -> (long) userService.getUsersByRole(role).size()
                    ));
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of());
        }
    }

    // ✅ 8. Xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 9. Health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "Đang hoạt động",
                "phase", "User API - Unified",
                "version", "2.0.0"
        ));
    }
}
