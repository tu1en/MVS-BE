package com.classroomapp.classroombackend.service.firebase;

import com.classroomapp.classroombackend.config.FirebaseClassroomConfig;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SlotDto;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Firebase Service cho Classroom Management
 * Xử lý real-time updates cho classrooms, sessions, và slots
 */
@Service
@Slf4j
public class FirebaseClassroomService {

    private static final String CLASSROOMS_PATH = "classrooms";
    private static final String SESSIONS_PATH = "sessions";
    private static final String SLOTS_PATH = "slots";
    private static final String LIVE_UPDATES_PATH = "live-updates";

    /**
     * Sync classroom data to Firebase
     */
    public CompletableFuture<Void> syncClassroom(ClassroomDto classroom) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!FirebaseClassroomConfig.isFirebaseAvailable()) {
                    log.debug("🔥 Firebase not available, skipping classroom sync");
                    return;
                }

                FirebaseDatabase database = FirebaseClassroomConfig.getDatabase();
                if (database == null) {
                    log.warn("🔥 Firebase database is null, skipping classroom sync");
                    return;
                }

                DatabaseReference classroomRef = database.getReference(CLASSROOMS_PATH)
                        .child(classroom.getId().toString());

                Map<String, Object> classroomData = new HashMap<>();
                classroomData.put("id", classroom.getId());
                classroomData.put("classroomName", classroom.getClassroomName());
                classroomData.put("description", classroom.getDescription());
                classroomData.put("teacherId", classroom.getTeacherId());
                classroomData.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                classroomRef.setValueAsync(classroomData);
                log.debug("🔥 Synced classroom {} to Firebase", classroom.getId());

            } catch (Exception e) {
                log.error("🔥 Failed to sync classroom {} to Firebase", classroom.getId(), e);
            }
        });
    }

    /**
     * Sync session data to Firebase
     */
    public CompletableFuture<Void> syncSession(SessionDto session) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!FirebaseClassroomConfig.isFirebaseAvailable()) {
                    log.debug("🔥 Firebase not available, skipping session sync");
                    return;
                }

                FirebaseDatabase database = FirebaseClassroomConfig.getDatabase();
                if (database == null) {
                    log.warn("🔥 Firebase database is null, skipping session sync");
                    return;
                }

                DatabaseReference sessionRef = database.getReference(SESSIONS_PATH)
                        .child(session.getId().toString());

                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("id", session.getId());
                sessionData.put("classroomId", session.getClassroomId());
                sessionData.put("sessionDate", session.getSessionDate().toString());
                sessionData.put("description", session.getDescription());
                sessionData.put("status", session.getStatus().toString());
                sessionData.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                sessionRef.setValueAsync(sessionData);
                log.debug("🔥 Synced session {} to Firebase", session.getId());

            } catch (Exception e) {
                log.error("🔥 Failed to sync session {} to Firebase", session.getId(), e);
            }
        });
    }

    /**
     * Sync slot data to Firebase
     */
    public CompletableFuture<Void> syncSlot(SlotDto slot) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!FirebaseClassroomConfig.isFirebaseAvailable()) {
                    log.debug("🔥 Firebase not available, skipping slot sync");
                    return;
                }

                FirebaseDatabase database = FirebaseClassroomConfig.getDatabase();
                if (database == null) {
                    log.warn("🔥 Firebase database is null, skipping slot sync");
                    return;
                }

                DatabaseReference slotRef = database.getReference(SLOTS_PATH)
                        .child(slot.getId().toString());

                Map<String, Object> slotData = new HashMap<>();
                slotData.put("id", slot.getId());
                slotData.put("slotName", slot.getSlotName());
                slotData.put("sessionId", slot.getSessionId());
                slotData.put("startTime", slot.getStartTime().toString());
                slotData.put("endTime", slot.getEndTime().toString());
                slotData.put("description", slot.getDescription());
                slotData.put("status", slot.getStatus().toString());
                slotData.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                slotRef.setValueAsync(slotData);
                log.debug("🔥 Synced slot {} to Firebase", slot.getId());

            } catch (Exception e) {
                log.error("🔥 Failed to sync slot {} to Firebase", slot.getId(), e);
            }
        });
    }

    /**
     * Remove classroom from Firebase
     */
    public CompletableFuture<Void> removeClassroom(Long classroomId) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!FirebaseClassroomConfig.isFirebaseAvailable()) {
                    log.debug("🔥 Firebase not available, skipping classroom removal");
                    return;
                }

                FirebaseDatabase database = FirebaseClassroomConfig.getDatabase();
                if (database == null) {
                    log.warn("🔥 Firebase database is null, skipping classroom removal");
                    return;
                }

                DatabaseReference classroomRef = database.getReference(CLASSROOMS_PATH)
                        .child(classroomId.toString());

                classroomRef.removeValueAsync();
                log.debug("🔥 Removed classroom {} from Firebase", classroomId);

            } catch (Exception e) {
                log.error("🔥 Failed to remove classroom {} from Firebase", classroomId, e);
            }
        });
    }

    /**
     * Remove session from Firebase
     */
    public CompletableFuture<Void> removeSession(Long sessionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!FirebaseClassroomConfig.isFirebaseAvailable()) {
                    log.debug("🔥 Firebase not available, skipping session removal");
                    return;
                }

                FirebaseDatabase database = FirebaseClassroomConfig.getDatabase();
                if (database == null) {
                    log.warn("🔥 Firebase database is null, skipping session removal");
                    return;
                }

                DatabaseReference sessionRef = database.getReference(SESSIONS_PATH)
                        .child(sessionId.toString());

                sessionRef.removeValueAsync();
                log.debug("🔥 Removed session {} from Firebase", sessionId);

            } catch (Exception e) {
                log.error("🔥 Failed to remove session {} from Firebase", sessionId, e);
            }
        });
    }

    /**
     * Remove slot from Firebase
     */
    public CompletableFuture<Void> removeSlot(Long slotId) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!FirebaseClassroomConfig.isFirebaseAvailable()) {
                    log.debug("🔥 Firebase not available, skipping slot removal");
                    return;
                }

                FirebaseDatabase database = FirebaseClassroomConfig.getDatabase();
                if (database == null) {
                    log.warn("🔥 Firebase database is null, skipping slot removal");
                    return;
                }

                DatabaseReference slotRef = database.getReference(SLOTS_PATH)
                        .child(slotId.toString());

                slotRef.removeValueAsync();
                log.debug("🔥 Removed slot {} from Firebase", slotId);

            } catch (Exception e) {
                log.error("🔥 Failed to remove slot {} from Firebase", slotId, e);
            }
        });
    }

    /**
     * Send live update notification
     */
    public CompletableFuture<Void> sendLiveUpdate(String type, Object data) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!FirebaseClassroomConfig.isFirebaseAvailable()) {
                    log.debug("🔥 Firebase not available, skipping live update");
                    return;
                }

                FirebaseDatabase database = FirebaseClassroomConfig.getDatabase();
                if (database == null) {
                    log.warn("🔥 Firebase database is null, skipping live update");
                    return;
                }

                DatabaseReference updateRef = database.getReference(LIVE_UPDATES_PATH)
                        .push();

                Map<String, Object> updateData = new HashMap<>();
                updateData.put("type", type);
                updateData.put("data", data);
                updateData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                updateRef.setValueAsync(updateData);
                log.debug("🔥 Sent live update: {}", type);

            } catch (Exception e) {
                log.error("🔥 Failed to send live update: {}", type, e);
            }
        });
    }
}
