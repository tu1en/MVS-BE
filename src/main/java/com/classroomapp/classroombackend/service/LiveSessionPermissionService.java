package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.classroomapp.classroombackend.model.LiveSessionPermission;
import com.classroomapp.classroombackend.model.LiveSessionPermission.PermissionType;

/**
 * Service để quản lý permissions cho live sessions
 */
public interface LiveSessionPermissionService {
    
    /**
     * Grant permission cho student trong live session
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @param teacherId ID của teacher grant permission
     * @param permissionType Loại permission
     * @param expiresAt Thời gian hết hạn (optional)
     * @param reason Lý do grant permission
     * @return LiveSessionPermission đã được tạo/cập nhật
     */
    LiveSessionPermission grantPermission(Long liveStreamId, Long studentId, Long teacherId,
                                        PermissionType permissionType, LocalDateTime expiresAt, String reason);
    
    /**
     * Deny permission cho student
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @param teacherId ID của teacher
     * @param permissionType Loại permission
     * @param reason Lý do deny
     * @return LiveSessionPermission đã được cập nhật
     */
    LiveSessionPermission denyPermission(Long liveStreamId, Long studentId, Long teacherId,
                                       PermissionType permissionType, String reason);
    
    /**
     * Revoke permission từ student
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @param teacherId ID của teacher
     * @param permissionType Loại permission
     * @param reason Lý do revoke
     * @return LiveSessionPermission đã được cập nhật
     */
    LiveSessionPermission revokePermission(Long liveStreamId, Long studentId, Long teacherId,
                                         PermissionType permissionType, String reason);
    
    /**
     * Check xem student có permission cụ thể không
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @param permissionType Loại permission
     * @return true nếu có permission
     */
    boolean hasPermission(Long liveStreamId, Long studentId, PermissionType permissionType);
    
    /**
     * Lấy tất cả permissions của student trong live session
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @return List permissions
     */
    List<LiveSessionPermission> getStudentPermissions(Long liveStreamId, Long studentId);
    
    /**
     * Lấy tất cả students và permissions trong live session
     * @param liveStreamId ID của live stream
     * @return Map với student info và permissions
     */
    List<Map<String, Object>> getAllStudentPermissions(Long liveStreamId);
    
    /**
     * Set default permissions cho student khi join live session
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @return List default permissions đã được tạo
     */
    List<LiveSessionPermission> setDefaultPermissions(Long liveStreamId, Long studentId);
    
    /**
     * Grant permission cho tất cả students trong session
     * @param liveStreamId ID của live stream
     * @param teacherId ID của teacher
     * @param permissionType Loại permission
     * @param expiresAt Thời gian hết hạn
     * @param reason Lý do
     * @return Số lượng permissions đã được grant
     */
    int grantPermissionToAll(Long liveStreamId, Long teacherId, PermissionType permissionType,
                           LocalDateTime expiresAt, String reason);
    
    /**
     * Revoke permission từ tất cả students
     * @param liveStreamId ID của live stream
     * @param teacherId ID của teacher
     * @param permissionType Loại permission
     * @param reason Lý do
     * @return Số lượng permissions đã được revoke
     */
    int revokePermissionFromAll(Long liveStreamId, Long teacherId, PermissionType permissionType, String reason);
    
    /**
     * Cleanup expired permissions
     * @return Số lượng permissions đã được cleanup
     */
    int cleanupExpiredPermissions();
    
    /**
     * Lấy permission statistics cho live session
     * @param liveStreamId ID của live stream
     * @return Map với statistics
     */
    Map<String, Object> getPermissionStatistics(Long liveStreamId);
    
    /**
     * Handle student request permission
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @param permissionType Loại permission
     * @param reason Lý do request
     * @return LiveSessionPermission với status PENDING
     */
    LiveSessionPermission requestPermission(Long liveStreamId, Long studentId, PermissionType permissionType, String reason);
    
    /**
     * Lấy pending permission requests cho teacher
     * @param liveStreamId ID của live stream
     * @return List pending requests
     */
    List<LiveSessionPermission> getPendingPermissionRequests(Long liveStreamId);
    
    /**
     * Approve hoặc deny pending permission request
     * @param permissionId ID của permission request
     * @param teacherId ID của teacher
     * @param approve true để approve, false để deny
     * @param reason Lý do
     * @param expiresAt Thời gian hết hạn nếu approve
     * @return LiveSessionPermission đã được cập nhật
     */
    LiveSessionPermission handlePermissionRequest(Long permissionId, Long teacherId, boolean approve,
                                                String reason, LocalDateTime expiresAt);
}