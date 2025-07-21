package com.classroomapp.classroombackend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.LiveStreamDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.service.LiveSessionAttendanceService;

/**
 * Controller để quản lý attendance tracking cho live sessions
 */
@RestController
@RequestMapping("/api/live-session-attendance")
public class LiveSessionAttendanceController {
    
    @Autowired
    private LiveSessionAttendanceService liveSessionAttendanceService;
    
    /**
     * Tạo attendance session cho live stream
     * Chỉ teacher có thể tạo
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AttendanceSession> createAttendanceSession(@RequestBody LiveStreamDto liveStreamDto) {
        try {
            AttendanceSession session = liveSessionAttendanceService.createAttendanceSessionForLiveStream(liveStreamDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Đánh dấu student tham gia live session
     * Students có thể tự đánh dấu hoặc hệ thống tự động đánh dấu
     */
    @PostMapping("/mark-joined/{liveStreamId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> markStudentJoined(
            @PathVariable Long liveStreamId,
            @RequestParam Long studentId,
            @RequestParam(required = false) String joinTimeStr) {
        try {
            LocalDateTime joinTime = joinTimeStr != null ? 
                    LocalDateTime.parse(joinTimeStr) : LocalDateTime.now();
            
            Map<String, Object> result = liveSessionAttendanceService
                    .markStudentJoinedLiveSession(liveStreamId, studentId, joinTime);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Đánh dấu student rời khỏi live session
     */
    @PostMapping("/mark-left/{liveStreamId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> markStudentLeft(
            @PathVariable Long liveStreamId,
            @RequestParam Long studentId,
            @RequestParam(required = false) String leaveTimeStr) {
        try {
            LocalDateTime leaveTime = leaveTimeStr != null ? 
                    LocalDateTime.parse(leaveTimeStr) : LocalDateTime.now();
            
            Map<String, Object> result = liveSessionAttendanceService
                    .markStudentLeftLiveSession(liveStreamId, studentId, leaveTime);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Kết thúc attendance session cho live stream
     * Chỉ teacher có thể kết thúc
     */
    @PostMapping("/end/{liveStreamId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AttendanceSession> endAttendanceSession(@PathVariable Long liveStreamId) {
        try {
            AttendanceSession session = liveSessionAttendanceService.endAttendanceSessionForLiveStream(liveStreamId);
            if (session != null) {
                return ResponseEntity.ok(session);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy danh sách attendance cho live session
     * Teachers có thể xem tất cả, students chỉ xem của mình
     */
    @GetMapping("/{liveStreamId}/attendance")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceForLiveSession(@PathVariable Long liveStreamId) {
        try {
            List<Map<String, Object>> attendance = liveSessionAttendanceService
                    .getAttendanceForLiveSession(liveStreamId);
            return ResponseEntity.ok(attendance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê attendance rate cho live session
     * Chỉ teacher có thể xem
     */
    @GetMapping("/{liveStreamId}/attendance-rate")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getAttendanceRate(@PathVariable Long liveStreamId) {
        try {
            Map<String, Object> rate = liveSessionAttendanceService
                    .calculateAttendanceRateForLiveSession(liveStreamId);
            return ResponseEntity.ok(rate);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy attendance session theo live stream ID
     */
    @GetMapping("/{liveStreamId}/session")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<AttendanceSession> getAttendanceSession(@PathVariable Long liveStreamId) {
        try {
            AttendanceSession session = liveSessionAttendanceService
                    .getAttendanceSessionByLiveStreamId(liveStreamId);
            if (session != null) {
                return ResponseEntity.ok(session);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Cập nhật attendance session với dữ liệu từ live stream
     * Chỉ teacher có thể cập nhật
     */
    @PutMapping("/{liveStreamId}/update")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AttendanceSession> updateAttendanceSession(
            @PathVariable Long liveStreamId,
            @RequestBody Map<String, Object> attendanceData) {
        try {
            AttendanceSession session = liveSessionAttendanceService
                    .updateAttendanceSessionFromLiveStream(liveStreamId, attendanceData);
            return ResponseEntity.ok(session);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}