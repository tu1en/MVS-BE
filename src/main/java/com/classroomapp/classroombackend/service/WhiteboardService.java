package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.classroomapp.classroombackend.model.WhiteboardData;

/**
 * Service Ä‘á»ƒ quáº£n lÃ½ whiteboard operations cho live sessions
 */
public interface WhiteboardService {
    
    /**
     * ThÃªm drawing operation vÃ o whiteboard
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user thá»±c hiá»‡n drawing
     * @param drawingData Dá»¯ liá»‡u drawing operation
     * @return WhiteboardData Ä‘Ã£ Ä‘Æ°á»£c lÆ°u
     */
    WhiteboardData addDrawingOperation(Long liveStreamId, Long userId, Map<String, Object> drawingData);
    
    /**
     * Cáº­p nháº­t element trÃªn whiteboard
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user
     * @param elementId ID cá»§a element
     * @param updateData Dá»¯ liá»‡u cáº­p nháº­t
     * @return WhiteboardData Ä‘Ã£ Ä‘Æ°á»£c cáº­p nháº­t
     */
    WhiteboardData updateElement(Long liveStreamId, Long userId, String elementId, Map<String, Object> updateData);
    
    /**
     * XÃ³a element khá»i whiteboard
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user
     * @param elementId ID cá»§a element cáº§n xÃ³a
     * @return true náº¿u xÃ³a thÃ nh cÃ´ng
     */
    boolean deleteElement(Long liveStreamId, Long userId, String elementId);
    
    /**
     * Clear toÃ n bá»™ whiteboard
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user (chá»‰ teacher má»›i Ä‘Æ°á»£c clear)
     * @return sá»‘ lÆ°á»£ng elements Ä‘Ã£ Ä‘Æ°á»£c xÃ³a
     */
    int clearWhiteboard(Long liveStreamId, Long userId);
    
    /**
     * Láº¥y toÃ n bá»™ whiteboard state hiá»‡n táº¡i
     * @param liveStreamId ID cá»§a live stream
     * @return List táº¥t cáº£ elements Ä‘ang active
     */
    List<WhiteboardData> getWhiteboardState(Long liveStreamId);
    
    /**
     * Láº¥y changes tá»« sequence number cá»¥ thá»ƒ
     * @param liveStreamId ID cá»§a live stream
     * @param fromSequence Sequence number báº¯t Ä‘áº§u
     * @return List changes tá»« sequence Ä‘Ã³
     */
    List<WhiteboardData> getChangesFromSequence(Long liveStreamId, Long fromSequence);
    
    /**
     * Láº¥y recent changes tá»« thá»i Ä‘iá»ƒm cá»¥ thá»ƒ
     * @param liveStreamId ID cá»§a live stream
     * @param since Thá»i Ä‘iá»ƒm báº¯t Ä‘áº§u
     * @return List recent changes
     */
    List<WhiteboardData> getRecentChanges(Long liveStreamId, LocalDateTime since);
    
    /**
     * Save whiteboard snapshot
     * @param liveStreamId ID cá»§a live stream
     * @return Map vá»›i snapshot data
     */
    Map<String, Object> saveWhiteboardSnapshot(Long liveStreamId);
    
    /**
     * Load whiteboard tá»« snapshot
     * @param liveStreamId ID cá»§a live stream
     * @param snapshotData Snapshot data
     * @return sá»‘ lÆ°á»£ng elements Ä‘Ã£ Ä‘Æ°á»£c restore
     */
    int loadWhiteboardFromSnapshot(Long liveStreamId, Map<String, Object> snapshotData);
    
    /**
     * Láº¥y whiteboard statistics
     * @param liveStreamId ID cá»§a live stream
     * @return Map vá»›i statistics
     */
    Map<String, Object> getWhiteboardStatistics(Long liveStreamId);
    
    /**
     * Export whiteboard data
     * @param liveStreamId ID cá»§a live stream
     * @param format Export format (JSON, SVG, PNG)
     * @return Export data
     */
    Map<String, Object> exportWhiteboardData(Long liveStreamId, String format);
    
    /**
     * Check permission Ä‘á»ƒ drawing trÃªn whiteboard
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user
     * @return true náº¿u cÃ³ permission
     */
    boolean hasDrawingPermission(Long liveStreamId, Long userId);
    
    /**
     * Undo last operation cá»§a user
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user
     * @return true náº¿u undo thÃ nh cÃ´ng
     */
    boolean undoLastOperation(Long liveStreamId, Long userId);
    
    /**
     * Redo operation cá»§a user
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user
     * @return true náº¿u redo thÃ nh cÃ´ng
     */
    boolean redoOperation(Long liveStreamId, Long userId);
    
    /**
     * Táº¡o collaborative drawing session
     * @param liveStreamId ID cá»§a live stream
     * @param teacherId ID cá»§a teacher
     * @param settings Session settings
     * @return Session data
     */
    Map<String, Object> createCollaborativeSession(Long liveStreamId, Long teacherId, Map<String, Object> settings);
    
    /**
     * End collaborative drawing session
     * @param liveStreamId ID cá»§a live stream
     * @param teacherId ID cá»§a teacher
     * @return Final session data
     */
    Map<String, Object> endCollaborativeSession(Long liveStreamId, Long teacherId);
    
    /**
     * Handle real-time drawing broadcast
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user
     * @param drawingEvent Real-time drawing event
     */
    void broadcastDrawingEvent(Long liveStreamId, Long userId, Map<String, Object> drawingEvent);
    
    /**
     * Cleanup old whiteboard data
     * @param olderThan XÃ³a data cÅ© hÆ¡n thá»i Ä‘iá»ƒm nÃ y
     * @return sá»‘ lÆ°á»£ng records Ä‘Ã£ Ä‘Æ°á»£c xÃ³a
     */
    int cleanupOldWhiteboardData(LocalDateTime olderThan);
}
