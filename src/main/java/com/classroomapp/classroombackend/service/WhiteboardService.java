package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.classroomapp.classroombackend.model.WhiteboardData;

/**
 * Service để quản lý whiteboard operations cho live sessions
 */
public interface WhiteboardService {
    
    /**
     * Thêm drawing operation vào whiteboard
     * @param liveStreamId ID của live stream
     * @param userId ID của user thực hiện drawing
     * @param drawingData Dữ liệu drawing operation
     * @return WhiteboardData đã được lưu
     */
    WhiteboardData addDrawingOperation(Long liveStreamId, Long userId, Map<String, Object> drawingData);
    
    /**
     * Cập nhật element trên whiteboard
     * @param liveStreamId ID của live stream
     * @param userId ID của user
     * @param elementId ID của element
     * @param updateData Dữ liệu cập nhật
     * @return WhiteboardData đã được cập nhật
     */
    WhiteboardData updateElement(Long liveStreamId, Long userId, String elementId, Map<String, Object> updateData);
    
    /**
     * Xóa element khỏi whiteboard
     * @param liveStreamId ID của live stream
     * @param userId ID của user
     * @param elementId ID của element cần xóa
     * @return true nếu xóa thành công
     */
    boolean deleteElement(Long liveStreamId, Long userId, String elementId);
    
    /**
     * Clear toàn bộ whiteboard
     * @param liveStreamId ID của live stream
     * @param userId ID của user (chỉ teacher mới được clear)
     * @return số lượng elements đã được xóa
     */
    int clearWhiteboard(Long liveStreamId, Long userId);
    
    /**
     * Lấy toàn bộ whiteboard state hiện tại
     * @param liveStreamId ID của live stream
     * @return List tất cả elements đang active
     */
    List<WhiteboardData> getWhiteboardState(Long liveStreamId);
    
    /**
     * Lấy changes từ sequence number cụ thể
     * @param liveStreamId ID của live stream
     * @param fromSequence Sequence number bắt đầu
     * @return List changes từ sequence đó
     */
    List<WhiteboardData> getChangesFromSequence(Long liveStreamId, Long fromSequence);
    
    /**
     * Lấy recent changes từ thời điểm cụ thể
     * @param liveStreamId ID của live stream
     * @param since Thời điểm bắt đầu
     * @return List recent changes
     */
    List<WhiteboardData> getRecentChanges(Long liveStreamId, LocalDateTime since);
    
    /**
     * Save whiteboard snapshot
     * @param liveStreamId ID của live stream
     * @return Map với snapshot data
     */
    Map<String, Object> saveWhiteboardSnapshot(Long liveStreamId);
    
    /**
     * Load whiteboard từ snapshot
     * @param liveStreamId ID của live stream
     * @param snapshotData Snapshot data
     * @return số lượng elements đã được restore
     */
    int loadWhiteboardFromSnapshot(Long liveStreamId, Map<String, Object> snapshotData);
    
    /**
     * Lấy whiteboard statistics
     * @param liveStreamId ID của live stream
     * @return Map với statistics
     */
    Map<String, Object> getWhiteboardStatistics(Long liveStreamId);
    
    /**
     * Export whiteboard data
     * @param liveStreamId ID của live stream
     * @param format Export format (JSON, SVG, PNG)
     * @return Export data
     */
    Map<String, Object> exportWhiteboardData(Long liveStreamId, String format);
    
    /**
     * Check permission để drawing trên whiteboard
     * @param liveStreamId ID của live stream
     * @param userId ID của user
     * @return true nếu có permission
     */
    boolean hasDrawingPermission(Long liveStreamId, Long userId);
    
    /**
     * Undo last operation của user
     * @param liveStreamId ID của live stream
     * @param userId ID của user
     * @return true nếu undo thành công
     */
    boolean undoLastOperation(Long liveStreamId, Long userId);
    
    /**
     * Redo operation của user
     * @param liveStreamId ID của live stream
     * @param userId ID của user
     * @return true nếu redo thành công
     */
    boolean redoOperation(Long liveStreamId, Long userId);
    
    /**
     * Tạo collaborative drawing session
     * @param liveStreamId ID của live stream
     * @param teacherId ID của teacher
     * @param settings Session settings
     * @return Session data
     */
    Map<String, Object> createCollaborativeSession(Long liveStreamId, Long teacherId, Map<String, Object> settings);
    
    /**
     * End collaborative drawing session
     * @param liveStreamId ID của live stream
     * @param teacherId ID của teacher
     * @return Final session data
     */
    Map<String, Object> endCollaborativeSession(Long liveStreamId, Long teacherId);
    
    /**
     * Handle real-time drawing broadcast
     * @param liveStreamId ID của live stream
     * @param userId ID của user
     * @param drawingEvent Real-time drawing event
     */
    void broadcastDrawingEvent(Long liveStreamId, Long userId, Map<String, Object> drawingEvent);
    
    /**
     * Cleanup old whiteboard data
     * @param olderThan Xóa data cũ hơn thời điểm này
     * @return số lượng records đã được xóa
     */
    int cleanupOldWhiteboardData(LocalDateTime olderThan);
}