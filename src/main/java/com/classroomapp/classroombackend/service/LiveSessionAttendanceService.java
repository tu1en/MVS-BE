package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.classroomapp.classroombackend.dto.LiveStreamDto;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;

/**
 * Service Ä‘á»ƒ tÃ­ch há»£p attendance tracking vá»›i live sessions
 */
public interface LiveSessionAttendanceService {
    
    /**
     * Tá»± Ä‘á»™ng táº¡o attendance session khi báº¯t Ä‘áº§u live stream
     * @param liveStreamDto Live stream information
     * @return AttendanceSession Ä‘Æ°á»£c táº¡o
     */
    AttendanceSession createAttendanceSessionForLiveStream(LiveStreamDto liveStreamDto);
    
    /**
     * ÄÃ¡nh dáº¥u student Ä‘Ã£ tham gia live session (tá»± Ä‘á»™ng attendance)
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @param joinTime Thá»i gian tham gia
     * @return Map vá»›i thÃ´ng tin attendance
     */
    Map<String, Object> markStudentJoinedLiveSession(Long liveStreamId, Long studentId, LocalDateTime joinTime);
    
    /**
     * ÄÃ¡nh dáº¥u student Ä‘Ã£ rá»i khá»i live session
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @param leaveTime Thá»i gian rá»i khá»i
     * @return Map vá»›i thÃ´ng tin attendance
     */
    Map<String, Object> markStudentLeftLiveSession(Long liveStreamId, Long studentId, LocalDateTime leaveTime);
    
    /**
     * Káº¿t thÃºc attendance session khi live stream káº¿t thÃºc
     * @param liveStreamId ID cá»§a live stream
     * @return AttendanceSession Ä‘Ã£ káº¿t thÃºc
     */
    AttendanceSession endAttendanceSessionForLiveStream(Long liveStreamId);
    
    /**
     * Láº¥y danh sÃ¡ch students Ä‘Ã£ tham gia live session
     * @param liveStreamId ID cá»§a live stream
     * @return List students vá»›i thÃ´ng tin attendance
     */
    List<Map<String, Object>> getAttendanceForLiveSession(Long liveStreamId);
    
    /**
     * TÃ­nh toÃ¡n attendance rate cho live session
     * @param liveStreamId ID cá»§a live stream
     * @return Map vá»›i attendance statistics
     */
    Map<String, Object> calculateAttendanceRateForLiveSession(Long liveStreamId);
    
    /**
     * Láº¥y attendance session liÃªn káº¿t vá»›i live stream
     * @param liveStreamId ID cá»§a live stream
     * @return AttendanceSession náº¿u cÃ³
     */
    AttendanceSession getAttendanceSessionByLiveStreamId(Long liveStreamId);
    
    /**
     * Cáº­p nháº­t attendance session vá»›i thÃ´ng tin tá»« live stream
     * @param liveStreamId ID cá»§a live stream
     * @param attendanceData Dá»¯ liá»‡u attendance cáº§n cáº­p nháº­t
     * @return AttendanceSession Ä‘Ã£ cáº­p nháº­t
     */
    AttendanceSession updateAttendanceSessionFromLiveStream(Long liveStreamId, Map<String, Object> attendanceData);
}
