package com.classroomapp.classroombackend.repository.usermanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // Support lookup by either username or email (useful when Authentication.getName() returns email)
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Tìm user theo roleId
    List<User> findByRoleId(Integer roleId);

    // Tìm tất cả teachers (roleId = 2)
    @Query("SELECT u FROM User u WHERE u.roleId = 2 ORDER BY u.fullName ASC")
    List<User> findAllTeachers();

    // Tìm tất cả managers (roleId = 3) 
    @Query("SELECT u FROM User u WHERE u.roleId = 3 ORDER BY u.fullName ASC")
    List<User> findAllManagers();

    // Tìm user theo roleId và sắp xếp theo tên
    List<User> findByRoleIdOrderByFullNameAsc(Integer roleId);

    // Tìm teachers theo tên hoặc email
    @Query("SELECT u FROM User u WHERE u.roleId = 2 AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<User> findTeachersByNameOrEmail(@Param("name") String name);

    // Tìm user theo keyword và roleId
    @Query("SELECT u FROM User u WHERE (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND u.roleId = :roleId")
    List<User> findByKeywordAndRole(@Param("keyword") String keyword, @Param("roleId") Integer roleId);

    // Đếm user theo roleId
    long countByRoleId(Integer roleId);

    // Tìm user active theo roleId
    @Query("SELECT u FROM User u WHERE u.status = 'active' AND u.roleId = :roleId ORDER BY u.fullName ASC")
    List<User> findByIsActiveTrueAndRoleIdOrderByFullNameAsc(@Param("roleId") Integer roleId);

    @Query("SELECT u FROM User u WHERE u.id IN (SELECT c.createdBy FROM CourseTemplate c WHERE c.isActive = true)")
    List<User> findCreateCourseUsers();

    List<User> findByStatus(String status);

    List<User> findByDepartment(String department);

    List<User> findByRoleIdAndStatus(Integer roleId, String status);

    @Query("SELECT u FROM User u WHERE u.status = 'active'")
    List<User> findActiveUsers();

    @Query("SELECT u FROM User u WHERE u.roleId = 2 AND u.status = 'active'")
    List<User> findActiveTeachers();

    @Query("SELECT u FROM User u WHERE u.roleId = 1 AND u.status = 'active'")
    List<User> findActiveStudents();

    @Query("SELECT u FROM User u WHERE u.roleId = 5 AND u.status = 'active'")
    List<User> findActiveAccountants();

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword% OR u.username LIKE %:keyword%")
    List<User> searchUsersByName(@Param("keyword") String keyword);

    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email, Pageable pageable);

    List<User> findAllByRoleId(Long roleId);

    @Query("SELECT COUNT(DISTINCT e.user.id) FROM ClassroomEnrollment e WHERE e.classroom.id IN :classroomIds")
    long countStudentsByClassroomIds(@Param("classroomIds") List<Long> classroomIds);

    List<User> findByRoleIdIn(List<Integer> roleIds);

    List<User> findByEmailIn(List<String> emails);
}