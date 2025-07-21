package com.classroomapp.classroombackend.service.usermanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service interface cho quản lý người dùng
 * Chịu trách nhiệm cho các thao tác liên quan đến user management
 */
@Service
public interface UserService {

    /**
     * Tìm user theo email
     * 
     * @param email địa chỉ email
     * @return Optional chứa user nếu tìm thấy
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Tìm user theo ID
     * 
     * @param id ID của user
     * @return Optional chứa user nếu tìm thấy
     */
    Optional<User> findById(Long id);
    
    /**
     * Kiểm tra xem email có tồn tại không
     * 
     * @param email địa chỉ email
     * @return true nếu email đã tồn tại
     */
    boolean existsByEmail(String email);
    
    /**
     * Lưu hoặc cập nhật user
     * 
     * @param user entity user
     * @return user đã được lưu
     */
    User save(User user);
    
    /**
     * Đếm tổng số user trong hệ thống
     * 
     * @return tổng số user
     */
    long count();
}
