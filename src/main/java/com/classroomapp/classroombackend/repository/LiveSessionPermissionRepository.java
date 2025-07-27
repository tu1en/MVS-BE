package com.classroomapp.classroombackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.LiveSessionPermission;
import com.classroomapp.classroombackend.model.LiveStream;
import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * Repository cho LiveSessionPermission
 */
@Repository
public interface LiveSessionPermissionRepository extends JpaRepository<LiveSessionPermission, Long> {
    
    /**
     * TÃ¬m permission theo live stream, student vÃ  permission type
     */
    Optional<LiveSessionPermission> findByLiveStreamAndStudentAndPermissionType(
            LiveStream liveStream, User student, LiveSessionPermission.PermissionType permissionType);
    
    /**
     * TÃ¬m táº¥t cáº£ permissions cá»§a student trong live stream
     */
    List<LiveSessionPermission> findByLiveStreamAndStudent(LiveStream liveStream, User student);
    
    /**
     * TÃ¬m táº¥t cáº£ permissions trong live stream
     */
    List<LiveSessionPermission> findByLiveStream(LiveStream liveStream);
    
    /**
     * TÃ¬m permissions theo status
     */
    List<LiveSessionPermission> findByLiveStreamAndPermissionStatus(
            LiveStream liveStream, LiveSessionPermission.PermissionStatus status);
    
    /**
     * TÃ¬m active permissions cho student
     */
    @Query("SELECT p FROM LiveSessionPermission p WHERE p.liveStream = :liveStream " +
           "AND p.student = :student AND p.isActive = true " +
           "AND p.permissionStatus = 'GRANTED' " +
           "AND (p.expiresAt IS NULL OR p.expiresAt > CURRENT_TIMESTAMP)")
    List<LiveSessionPermission> findActivePermissionsForStudent(
            @Param("liveStream") LiveStream liveStream, @Param("student") User student);
    
    /**
     * TÃ¬m permissions theo permission type trong live stream
     */
    List<LiveSessionPermission> findByLiveStreamAndPermissionType(
            LiveStream liveStream, LiveSessionPermission.PermissionType permissionType);
    
    /**
     * TÃ¬m permissions Ä‘Ã£ expire
     */
    @Query("SELECT p FROM LiveSessionPermission p WHERE p.expiresAt < CURRENT_TIMESTAMP " +
           "AND p.permissionStatus = 'GRANTED' AND p.isActive = true")
    List<LiveSessionPermission> findExpiredPermissions();
    
    /**
     * Äáº¿m sá»‘ students cÃ³ permission type cá»¥ thá»ƒ trong live stream
     */
    @Query("SELECT COUNT(p) FROM LiveSessionPermission p WHERE p.liveStream = :liveStream " +
           "AND p.permissionType = :permissionType AND p.permissionStatus = 'GRANTED' " +
           "AND p.isActive = true AND (p.expiresAt IS NULL OR p.expiresAt > CURRENT_TIMESTAMP)")
    long countActivePermissionsByType(
            @Param("liveStream") LiveStream liveStream, 
            @Param("permissionType") LiveSessionPermission.PermissionType permissionType);
    
    /**
     * TÃ¬m permissions Ä‘Æ°á»£c grant bá»Ÿi teacher cá»¥ thá»ƒ
     */
    List<LiveSessionPermission> findByLiveStreamAndTeacher(LiveStream liveStream, User teacher);
    
    /**
     * Check if student cÃ³ permission cá»¥ thá»ƒ
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM LiveSessionPermission p WHERE p.liveStream = :liveStream " +
           "AND p.student = :student AND p.permissionType = :permissionType " +
           "AND p.permissionStatus = 'GRANTED' AND p.isActive = true " +
           "AND (p.expiresAt IS NULL OR p.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasActivePermission(
            @Param("liveStream") LiveStream liveStream,
            @Param("student") User student,
            @Param("permissionType") LiveSessionPermission.PermissionType permissionType);
    
    /**
     * XÃ³a táº¥t cáº£ permissions cá»§a live stream khi session káº¿t thÃºc
     */
    void deleteByLiveStream(LiveStream liveStream);
}
