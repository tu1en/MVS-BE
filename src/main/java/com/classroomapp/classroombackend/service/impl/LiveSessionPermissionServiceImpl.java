package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.LiveSessionPermission;
import com.classroomapp.classroombackend.model.LiveSessionPermission.PermissionStatus;
import com.classroomapp.classroombackend.model.LiveSessionPermission.PermissionType;
import com.classroomapp.classroombackend.model.LiveStream;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LiveSessionPermissionRepository;
import com.classroomapp.classroombackend.repository.LiveStreamRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.LiveSessionPermissionService;

/**
 * Implementation của LiveSessionPermissionService
 */
@Service
@Transactional
public class LiveSessionPermissionServiceImpl implements LiveSessionPermissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(LiveSessionPermissionServiceImpl.class);
    
    @Autowired
    private LiveSessionPermissionRepository permissionRepository;
    
    @Autowired
    private LiveStreamRepository liveStreamRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Default permissions khi student join session
    private static final List<PermissionType> DEFAULT_PERMISSIONS = Arrays.asList(
            PermissionType.CHAT_ENABLED,
            PermissionType.RAISE_HAND_ENABLED
    );
    
    @Override
    public LiveSessionPermission grantPermission(Long liveStreamId, Long studentId, Long teacherId,
                                               PermissionType permissionType, LocalDateTime expiresAt, String reason) {
        try {
            logger.info("Granting permission {} to student {} in live stream {}", 
                       permissionType, studentId, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User student = getUserById(studentId);
            User teacher = getUserById(teacherId);
            
            // Tìm existing permission hoặc tạo mới
            Optional<LiveSessionPermission> existingOpt = permissionRepository
                    .findByLiveStreamAndStudentAndPermissionType(liveStream, student, permissionType);
            
            LiveSessionPermission permission;
            if (existingOpt.isPresent()) {
                permission = existingOpt.get();
                permission.grantPermission(expiresAt, reason);
                permission.setTeacher(teacher);
            } else {
                permission = LiveSessionPermission.builder()
                        .liveStream(liveStream)
                        .student(student)
                        .teacher(teacher)
                        .permissionType(permissionType)
                        .permissionStatus(PermissionStatus.GRANTED)
                        .grantedAt(LocalDateTime.now())
                        .expiresAt(expiresAt)
                        .reason(reason)
                        .isActive(true)
                        .build();
            }
            
            LiveSessionPermission saved = permissionRepository.save(permission);
            logger.info("Granted permission {} to student {}", permissionType, studentId);
            return saved;
            
        } catch (Exception e) {
            logger.error("Error granting permission: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to grant permission", e);
        }
    }
    
    @Override
    public LiveSessionPermission denyPermission(Long liveStreamId, Long studentId, Long teacherId,
                                              PermissionType permissionType, String reason) {
        try {
            logger.info("Denying permission {} to student {} in live stream {}", 
                       permissionType, studentId, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User student = getUserById(studentId);
            User teacher = getUserById(teacherId);
            
            Optional<LiveSessionPermission> existingOpt = permissionRepository
                    .findByLiveStreamAndStudentAndPermissionType(liveStream, student, permissionType);
            
            LiveSessionPermission permission;
            if (existingOpt.isPresent()) {
                permission = existingOpt.get();
                permission.denyPermission(reason);
                permission.setTeacher(teacher);
            } else {
                permission = LiveSessionPermission.builder()
                        .liveStream(liveStream)
                        .student(student)
                        .teacher(teacher)
                        .permissionType(permissionType)
                        .permissionStatus(PermissionStatus.DENIED)
                        .reason(reason)
                        .isActive(true)
                        .build();
            }
            
            LiveSessionPermission saved = permissionRepository.save(permission);
            logger.info("Denied permission {} to student {}", permissionType, studentId);
            return saved;
            
        } catch (Exception e) {
            logger.error("Error denying permission: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deny permission", e);
        }
    }
    
    @Override
    public LiveSessionPermission revokePermission(Long liveStreamId, Long studentId, Long teacherId,
                                                PermissionType permissionType, String reason) {
        try {
            logger.info("Revoking permission {} from student {} in live stream {}", 
                       permissionType, studentId, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User student = getUserById(studentId);
            User teacher = getUserById(teacherId);
            
            Optional<LiveSessionPermission> existingOpt = permissionRepository
                    .findByLiveStreamAndStudentAndPermissionType(liveStream, student, permissionType);
            
            if (existingOpt.isPresent()) {
                LiveSessionPermission permission = existingOpt.get();
                permission.revokePermission(reason);
                permission.setTeacher(teacher);
                
                LiveSessionPermission saved = permissionRepository.save(permission);
                logger.info("Revoked permission {} from student {}", permissionType, studentId);
                return saved;
            } else {
                throw new ResourceNotFoundException("Permission not found");
            }
            
        } catch (Exception e) {
            logger.error("Error revoking permission: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to revoke permission", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long liveStreamId, Long studentId, PermissionType permissionType) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User student = getUserById(studentId);
            
            return permissionRepository.hasActivePermission(liveStream, student, permissionType);
            
        } catch (Exception e) {
            logger.error("Error checking permission: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionPermission> getStudentPermissions(Long liveStreamId, Long studentId) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User student = getUserById(studentId);
            
            return permissionRepository.findByLiveStreamAndStudent(liveStream, student);
            
        } catch (Exception e) {
            logger.error("Error getting student permissions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllStudentPermissions(Long liveStreamId) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            List<LiveSessionPermission> permissions = permissionRepository.findByLiveStream(liveStream);
            
            // Group permissions by student
            Map<Long, Map<String, Object>> studentMap = new HashMap<>();
            
            for (LiveSessionPermission permission : permissions) {
                Long studentId = permission.getStudent().getId();
                
                Map<String, Object> studentData = studentMap.computeIfAbsent(studentId, k -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("studentId", studentId);
                    data.put("studentName", permission.getStudent().getFullName());
                    data.put("permissions", new HashMap<String, Object>());
                    return data;
                });
                
                @SuppressWarnings("unchecked")
                Map<String, Object> permissionMap = (Map<String, Object>) studentData.get("permissions");
                
                Map<String, Object> permissionData = new HashMap<>();
                permissionData.put("status", permission.getPermissionStatus().toString());
                permissionData.put("grantedAt", permission.getGrantedAt());
                permissionData.put("expiresAt", permission.getExpiresAt());
                permissionData.put("reason", permission.getReason());
                permissionData.put("isActive", permission.isCurrentlyValid());
                
                permissionMap.put(permission.getPermissionType().toString(), permissionData);
            }
            
            return new ArrayList<>(studentMap.values());
            
        } catch (Exception e) {
            logger.error("Error getting all student permissions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<LiveSessionPermission> setDefaultPermissions(Long liveStreamId, Long studentId) {
        try {
            logger.info("Setting default permissions for student {} in live stream {}", studentId, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User student = getUserById(studentId);
            
            List<LiveSessionPermission> defaultPermissions = new ArrayList<>();
            
            for (PermissionType permissionType : DEFAULT_PERMISSIONS) {
                Optional<LiveSessionPermission> existingOpt = permissionRepository
                        .findByLiveStreamAndStudentAndPermissionType(liveStream, student, permissionType);
                
                if (!existingOpt.isPresent()) {
                    LiveSessionPermission permission = LiveSessionPermission.builder()
                            .liveStream(liveStream)
                            .student(student)
                            .teacher(liveStream.getLecture().getTeacher()) // Default to lecture teacher
                            .permissionType(permissionType)
                            .permissionStatus(PermissionStatus.GRANTED)
                            .grantedAt(LocalDateTime.now())
                            .reason("Default permission on join")
                            .isActive(true)
                            .build();
                    
                    defaultPermissions.add(permissionRepository.save(permission));
                }
            }
            
            logger.info("Set {} default permissions for student {}", defaultPermissions.size(), studentId);
            return defaultPermissions;
            
        } catch (Exception e) {
            logger.error("Error setting default permissions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to set default permissions", e);
        }
    }
    
    @Override
    public int grantPermissionToAll(Long liveStreamId, Long teacherId, PermissionType permissionType,
                                  LocalDateTime expiresAt, String reason) {
        try {
            logger.info("Granting permission {} to all students in live stream {}", permissionType, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User teacher = getUserById(teacherId);
            
            // Get all students in the live stream (simplification - could get from classroom enrollment)
            List<LiveSessionPermission> existingPermissions = permissionRepository
                    .findByLiveStreamAndPermissionType(liveStream, permissionType);
            
            int grantedCount = 0;
            
            // For now, grant to students who already have some permission records
            Map<Long, User> students = new HashMap<>();
            for (LiveSessionPermission perm : existingPermissions) {
                students.put(perm.getStudent().getId(), perm.getStudent());
            }
            
            for (User student : students.values()) {
                Optional<LiveSessionPermission> existingOpt = permissionRepository
                        .findByLiveStreamAndStudentAndPermissionType(liveStream, student, permissionType);
                
                LiveSessionPermission permission;
                if (existingOpt.isPresent()) {
                    permission = existingOpt.get();
                    permission.grantPermission(expiresAt, reason);
                    permission.setTeacher(teacher);
                } else {
                    permission = LiveSessionPermission.builder()
                            .liveStream(liveStream)
                            .student(student)
                            .teacher(teacher)
                            .permissionType(permissionType)
                            .permissionStatus(PermissionStatus.GRANTED)
                            .grantedAt(LocalDateTime.now())
                            .expiresAt(expiresAt)
                            .reason(reason)
                            .isActive(true)
                            .build();
                }
                
                permissionRepository.save(permission);
                grantedCount++;
            }
            
            logger.info("Granted permission {} to {} students", permissionType, grantedCount);
            return grantedCount;
            
        } catch (Exception e) {
            logger.error("Error granting permission to all: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to grant permission to all students", e);
        }
    }
    
    @Override
    public int revokePermissionFromAll(Long liveStreamId, Long teacherId, PermissionType permissionType, String reason) {
        try {
            logger.info("Revoking permission {} from all students in live stream {}", permissionType, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User teacher = getUserById(teacherId);
            
            List<LiveSessionPermission> permissions = permissionRepository
                    .findByLiveStreamAndPermissionType(liveStream, permissionType);
            
            int revokedCount = 0;
            
            for (LiveSessionPermission permission : permissions) {
                if (permission.getPermissionStatus() == PermissionStatus.GRANTED) {
                    permission.revokePermission(reason);
                    permission.setTeacher(teacher);
                    permissionRepository.save(permission);
                    revokedCount++;
                }
            }
            
            logger.info("Revoked permission {} from {} students", permissionType, revokedCount);
            return revokedCount;
            
        } catch (Exception e) {
            logger.error("Error revoking permission from all: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to revoke permission from all students", e);
        }
    }
    
    @Override
    public int cleanupExpiredPermissions() {
        try {
            List<LiveSessionPermission> expiredPermissions = permissionRepository.findExpiredPermissions();
            
            int cleanedCount = 0;
            for (LiveSessionPermission permission : expiredPermissions) {
                permission.setPermissionStatus(PermissionStatus.DENIED);
                permission.setIsActive(false);
                permission.setUpdatedAt(LocalDateTime.now());
                permissionRepository.save(permission);
                cleanedCount++;
            }
            
            logger.info("Cleaned up {} expired permissions", cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            logger.error("Error cleaning up expired permissions: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPermissionStatistics(Long liveStreamId) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            
            Map<String, Object> stats = new HashMap<>();
            
            for (PermissionType permissionType : PermissionType.values()) {
                long count = permissionRepository.countActivePermissionsByType(liveStream, permissionType);
                stats.put(permissionType.toString().toLowerCase() + "_count", count);
            }
            
            List<LiveSessionPermission> allPermissions = permissionRepository.findByLiveStream(liveStream);
            long totalStudents = allPermissions.stream()
                    .map(p -> p.getStudent().getId())
                    .distinct()
                    .count();
            
            stats.put("total_students", totalStudents);
            stats.put("total_permissions", allPermissions.size());
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting permission statistics: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    @Override
    public LiveSessionPermission requestPermission(Long liveStreamId, Long studentId, 
                                                 PermissionType permissionType, String reason) {
        try {
            logger.info("Student {} requesting permission {} in live stream {}", 
                       studentId, permissionType, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User student = getUserById(studentId);
            
            Optional<LiveSessionPermission> existingOpt = permissionRepository
                    .findByLiveStreamAndStudentAndPermissionType(liveStream, student, permissionType);
            
            LiveSessionPermission permission;
            if (existingOpt.isPresent()) {
                permission = existingOpt.get();
                permission.setPermissionStatus(PermissionStatus.PENDING);
                permission.setReason(reason);
                permission.setUpdatedAt(LocalDateTime.now());
            } else {
                permission = LiveSessionPermission.builder()
                        .liveStream(liveStream)
                        .student(student)
                        .teacher(liveStream.getLecture().getTeacher())
                        .permissionType(permissionType)
                        .permissionStatus(PermissionStatus.PENDING)
                        .reason(reason)
                        .isActive(true)
                        .build();
            }
            
            LiveSessionPermission saved = permissionRepository.save(permission);
            logger.info("Created permission request for student {}", studentId);
            return saved;
            
        } catch (Exception e) {
            logger.error("Error creating permission request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create permission request", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionPermission> getPendingPermissionRequests(Long liveStreamId) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            return permissionRepository.findByLiveStreamAndPermissionStatus(liveStream, PermissionStatus.PENDING);
            
        } catch (Exception e) {
            logger.error("Error getting pending permission requests: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public LiveSessionPermission handlePermissionRequest(Long permissionId, Long teacherId, boolean approve,
                                                       String reason, LocalDateTime expiresAt) {
        try {
            Optional<LiveSessionPermission> permissionOpt = permissionRepository.findById(permissionId);
            if (!permissionOpt.isPresent()) {
                throw new ResourceNotFoundException("Permission request not found: " + permissionId);
            }
            
            LiveSessionPermission permission = permissionOpt.get();
            User teacher = getUserById(teacherId);
            
            permission.setTeacher(teacher);
            permission.setReason(reason);
            permission.setUpdatedAt(LocalDateTime.now());
            
            if (approve) {
                permission.grantPermission(expiresAt, reason);
                logger.info("Approved permission request {} for student {}", 
                           permissionId, permission.getStudent().getId());
            } else {
                permission.denyPermission(reason);
                logger.info("Denied permission request {} for student {}", 
                           permissionId, permission.getStudent().getId());
            }
            
            return permissionRepository.save(permission);
            
        } catch (Exception e) {
            logger.error("Error handling permission request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to handle permission request", e);
        }
    }
    
    // Helper methods
    private LiveStream getLiveStreamById(Long liveStreamId) {
        return liveStreamRepository.findById(liveStreamId)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found: " + liveStreamId));
    }
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}