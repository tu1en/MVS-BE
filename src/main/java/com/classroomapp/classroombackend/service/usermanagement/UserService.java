package com.classroomapp.classroombackend.service.usermanagement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.usermanagement.UserDTO;
import com.classroomapp.classroombackend.model.enums.UserRole;
import com.classroomapp.classroombackend.model.usermanagement.User;

public interface UserService {

    // CRUD cơ bản
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    List<UserDTO> getUsersByRole(UserRole role);
    Page<UserDTO> findAllUsers(String searchTerm, Pageable pageable);

    // Create & Update
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long userId, UserDTO userDTO);
    UserDTO updateUserStatus(Long userId, Boolean enabled);
    UserDTO updateUserRoles(Long userId, Set<String> roles);
    void resetPassword(Long userId);

    // Xóa user
    void deleteUser(Long id);

    // Kiểm tra tồn tại
    boolean usernameExists(String username);
    boolean emailExists(String email);

    // Truy vấn entity
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByUsername(String username);

    // Lưu và đếm
    User save(User user);
    long count();

    // Email reset password
    void sendPasswordResetEmail(String email, String resetLink);
}
