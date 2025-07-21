package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.classroomapp.classroombackend.dto.LiveStreamDto;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;

/**
 * Service để tích hợp attendance tracking với live sessions
 */
public interface LiveSessionAttendanceService {
    
    /**
     * Tự động tạo attendance session khi bắt đầu live stream
     * @param liveStreamDto Live stream information
     * @return AttendanceSession được tạo
     */
    AttendanceSession createAttendanceSessionForLiveStream(LiveStreamDto liveStreamDto);
    
    /**
     * Đánh dấu student đã tham gia live session (tự động attendance)
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @param joinTime Thời gian tham gia
     * @return Map với thông tin attendance
     */
    Map<String, Object> markStudentJoinedLiveSession(Long liveStreamId, Long studentId, LocalDateTime joinTime);
    
    /**
     * Đánh dấu student đã rời khỏi live session
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @param leaveTime Thời gian rời khỏi
     * @return Map với thông tin attendance
     */
    Map<String, Object> markStudentLeftLiveSession(Long liveStreamId, Long studentId, LocalDateTime leaveTime);
    
    /**
     * Kết thúc attendance session khi live stream kết thúc
     * @param liveStreamId ID của live stream
     * @return AttendanceSession đã kết thúc
     */
    AttendanceSession endAttendanceSessionForLiveStream(Long liveStreamId);
    
    /**
     * Lấy danh sách students đã tham gia live session
     * @param liveStreamId ID của live stream
     * @return List students với thông tin attendance
     */
    List<Map<String, Object>> getAttendanceForLiveSession(Long liveStreamId);
    
    /**
     * Tính toán attendance rate cho live session
     * @param liveStreamId ID của live stream
     * @return Map với attendance statistics
     */
    Map<String, Object> calculateAttendanceRateForLiveSession(Long liveStreamId);
    
    /**
     * Lấy attendance session liên kết với live stream
     * @param liveStreamId ID của live stream
     * @return AttendanceSession nếu có
     */
    AttendanceSession getAttendanceSessionByLiveStreamId(Long liveStreamId);
    
    /**
     * Cập nhật attendance session với thông tin từ live stream
     * @param liveStreamId ID của live stream
     * @param attendanceData Dữ liệu attendance cần cập nhật
     * @return AttendanceSession đã cập nhật
     */
    AttendanceSession updateAttendanceSessionFromLiveStream(Long liveStreamId, Map<String, Object> attendanceData);
}