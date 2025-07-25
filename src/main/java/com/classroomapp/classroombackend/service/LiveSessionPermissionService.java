package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.classroomapp.classroombackend.model.LiveSessionPermission;
import com.classroomapp.classroombackend.model.LiveSessionPermission.PermissionType;

/**
 * Service Ä‘á»ƒ quáº£n lÃ½ permissions cho live sessions
 */
public interface LiveSessionPermissionService {
    
    /**
     * Grant permission cho student trong live session
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @param teacherId ID cá»§a teacher grant permission
     * @param permissionType Loáº¡i permission
     * @param expiresAt Thá»i gian háº¿t háº¡n (optional)
     * @param reason LÃ½ do grant permission
     * @return LiveSessionPermission Ä‘Ã£ Ä‘Æ°á»£c táº¡o/cáº­p nháº­t
     */
    LiveSessionPermission grantPermission(Long liveStreamId, Long studentId, Long teacherId,
                                        PermissionType permissionType, LocalDateTime expiresAt, String reason);
    
    /**
     * Deny permission cho student
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @param teacherId ID cá»§a teacher
     * @param permissionType Loáº¡i permission
     * @param reason LÃ½ do deny
     * @return LiveSessionPermission Ä‘Ã£ Ä‘Æ°á»£c cáº­p nháº­t
     */
    LiveSessionPermission denyPermission(Long liveStreamId, Long studentId, Long teacherId,
                                       PermissionType permissionType, String reason);
    
    /**
     * Revoke permission tá»« student
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @param teacherId ID cá»§a teacher
     * @param permissionType Loáº¡i permission
     * @param reason LÃ½ do revoke
     * @return LiveSessionPermission Ä‘Ã£ Ä‘Æ°á»£c cáº­p nháº­t
     */
    LiveSessionPermission revokePermission(Long liveStreamId, Long studentId, Long teacherId,
                                         PermissionType permissionType, String reason);
    
    /**
     * Check xem student cÃ³ permission cá»¥ thá»ƒ khÃ´ng
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @param permissionType Loáº¡i permission
     * @return true náº¿u cÃ³ permission
     */
    boolean hasPermission(Long liveStreamId, Long studentId, PermissionType permissionType);
    
    /**
     * Láº¥y táº¥t cáº£ permissions cá»§a student trong live session
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @return List permissions
     */
    List<LiveSessionPermission> getStudentPermissions(Long liveStreamId, Long studentId);
    
    /**
     * Láº¥y táº¥t cáº£ students vÃ  permissions trong live session
     * @param liveStreamId ID cá»§a live stream
     * @return Map vá»›i student info vÃ  permissions
     */
    List<Map<String, Object>> getAllStudentPermissions(Long liveStreamId);
    
    /**
     * Set default permissions cho student khi join live session
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @return List default permissions Ä‘Ã£ Ä‘Æ°á»£c táº¡o
     */
    List<LiveSessionPermission> setDefaultPermissions(Long liveStreamId, Long studentId);
    
    /**
     * Grant permission cho táº¥t cáº£ students trong session
     * @param liveStreamId ID cá»§a live stream
     * @param teacherId ID cá»§a teacher
     * @param permissionType Loáº¡i permission
     * @param expiresAt Thá»i gian háº¿t háº¡n
     * @param reason LÃ½ do
     * @return Sá»‘ lÆ°á»£ng permissions Ä‘Ã£ Ä‘Æ°á»£c grant
     */
    int grantPermissionToAll(Long liveStreamId, Long teacherId, PermissionType permissionType,
                           LocalDateTime expiresAt, String reason);
    
    /**
     * Revoke permission tá»« táº¥t cáº£ students
     * @param liveStreamId ID cá»§a live stream
     * @param teacherId ID cá»§a teacher
     * @param permissionType Loáº¡i permission
     * @param reason LÃ½ do
     * @return Sá»‘ lÆ°á»£ng permissions Ä‘Ã£ Ä‘Æ°á»£c revoke
     */
    int revokePermissionFromAll(Long liveStreamId, Long teacherId, PermissionType permissionType, String reason);
    
    /**
     * Cleanup expired permissions
     * @return Sá»‘ lÆ°á»£ng permissions Ä‘Ã£ Ä‘Æ°á»£c cleanup
     */
    int cleanupExpiredPermissions();
    
    /**
     * Láº¥y permission statistics cho live session
     * @param liveStreamId ID cá»§a live stream
     * @return Map vá»›i statistics
     */
    Map<String, Object> getPermissionStatistics(Long liveStreamId);
    
    /**
     * Handle student request permission
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @param permissionType Loáº¡i permission
     * @param reason LÃ½ do request
     * @return LiveSessionPermission vá»›i status PENDING
     */
    LiveSessionPermission requestPermission(Long liveStreamId, Long studentId, PermissionType permissionType, String reason);
    
    /**
     * Láº¥y pending permission requests cho teacher
     * @param liveStreamId ID cá»§a live stream
     * @return List pending requests
     */
    List<LiveSessionPermission> getPendingPermissionRequests(Long liveStreamId);
    
    /**
     * Approve hoáº·c deny pending permission request
     * @param permissionId ID cá»§a permission request
     * @param teacherId ID cá»§a teacher
     * @param approve true Ä‘á»ƒ approve, false Ä‘á»ƒ deny
     * @param reason LÃ½ do
     * @param expiresAt Thá»i gian háº¿t háº¡n náº¿u approve
     * @return LiveSessionPermission Ä‘Ã£ Ä‘Æ°á»£c cáº­p nháº­t
     */
    LiveSessionPermission handlePermissionRequest(Long permissionId, Long teacherId, boolean approve,
                                                String reason, LocalDateTime expiresAt);
}
