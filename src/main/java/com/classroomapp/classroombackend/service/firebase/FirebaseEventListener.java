package com.classroomapp.classroombackend.service.firebase;

import com.classroomapp.classroombackend.config.FirebaseClassroomConfig;
import com.google.firebase.database.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * Firebase Event Listener cho Real-time Updates
 * Lắng nghe các thay đổi từ Firebase và gửi notifications qua WebSocket
 */
@Service
@Slf4j
public class FirebaseEventListener {

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    private DatabaseReference classroomsRef;
    private DatabaseReference sessionsRef;
    private DatabaseReference slotsRef;
    private DatabaseReference liveUpdatesRef;

    private final Map<String, ValueEventListener> listeners = new HashMap<>();

    @PostConstruct
    public void initializeListeners() {
        try {
            if (!FirebaseClassroomConfig.isFirebaseAvailable()) {
                log.info("🔥 Firebase not available, skipping event listeners initialization");
                return;
            }

            FirebaseDatabase database = FirebaseClassroomConfig.getDatabase();
            if (database == null) {
                log.warn("🔥 Firebase database is null, skipping event listeners initialization");
                return;
            }

            // Initialize references
            classroomsRef = database.getReference("classrooms");
            sessionsRef = database.getReference("sessions");
            slotsRef = database.getReference("slots");
            liveUpdatesRef = database.getReference("live-updates");

            // Setup listeners
            setupClassroomListener();
            setupSessionListener();
            setupSlotListener();
            setupLiveUpdatesListener();

            log.info("🔥 Firebase event listeners initialized successfully");

        } catch (Exception e) {
            log.error("🔥 Failed to initialize Firebase event listeners", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            // Remove all listeners
            listeners.forEach((path, listener) -> {
                try {
                    FirebaseDatabase database = FirebaseClassroomConfig.getDatabase();
                    if (database != null) {
                        database.getReference(path).removeEventListener(listener);
                    }
                } catch (Exception e) {
                    log.warn("🔥 Failed to remove listener for path: {}", path, e);
                }
            });
            listeners.clear();
            log.info("🔥 Firebase event listeners cleaned up");
        } catch (Exception e) {
            log.error("🔥 Error during Firebase listeners cleanup", e);
        }
    }

    private void setupClassroomListener() {
        if (classroomsRef == null) return;

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    log.debug("🔥 Classroom data changed: {}", dataSnapshot.getKey());
                    
                    if (messagingTemplate != null) {
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", "classroom_update");
                        message.put("data", dataSnapshot.getValue());
                        message.put("timestamp", System.currentTimeMillis());
                        
                        messagingTemplate.convertAndSend("/topic/classroom-updates", message);
                    }
                } catch (Exception e) {
                    log.error("🔥 Error processing classroom data change", e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                log.error("🔥 Classroom listener cancelled: {}", databaseError.getMessage());
            }
        };

        classroomsRef.addValueEventListener(listener);
        listeners.put("classrooms", listener);
    }

    private void setupSessionListener() {
        if (sessionsRef == null) return;

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    log.debug("🔥 Session data changed: {}", dataSnapshot.getKey());
                    
                    if (messagingTemplate != null) {
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", "session_update");
                        message.put("data", dataSnapshot.getValue());
                        message.put("timestamp", System.currentTimeMillis());
                        
                        messagingTemplate.convertAndSend("/topic/session-updates", message);
                    }
                } catch (Exception e) {
                    log.error("🔥 Error processing session data change", e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                log.error("🔥 Session listener cancelled: {}", databaseError.getMessage());
            }
        };

        sessionsRef.addValueEventListener(listener);
        listeners.put("sessions", listener);
    }

    private void setupSlotListener() {
        if (slotsRef == null) return;

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    log.debug("🔥 Slot data changed: {}", dataSnapshot.getKey());
                    
                    if (messagingTemplate != null) {
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", "slot_update");
                        message.put("data", dataSnapshot.getValue());
                        message.put("timestamp", System.currentTimeMillis());
                        
                        messagingTemplate.convertAndSend("/topic/slot-updates", message);
                    }
                } catch (Exception e) {
                    log.error("🔥 Error processing slot data change", e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                log.error("🔥 Slot listener cancelled: {}", databaseError.getMessage());
            }
        };

        slotsRef.addValueEventListener(listener);
        listeners.put("slots", listener);
    }

    private void setupLiveUpdatesListener() {
        if (liveUpdatesRef == null) return;

        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                try {
                    log.debug("🔥 Live update added: {}", dataSnapshot.getKey());
                    
                    if (messagingTemplate != null) {
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", "live_update");
                        message.put("data", dataSnapshot.getValue());
                        message.put("timestamp", System.currentTimeMillis());
                        
                        messagingTemplate.convertAndSend("/topic/live-updates", message);
                        
                        // Clean up old live updates (keep only last 100)
                        cleanupOldLiveUpdates();
                    }
                } catch (Exception e) {
                    log.error("🔥 Error processing live update", e);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // Not used for live updates
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Not used for live updates
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Not used for live updates
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                log.error("🔥 Live updates listener cancelled: {}", databaseError.getMessage());
            }
        };

        liveUpdatesRef.addChildEventListener(listener);
    }

    private void cleanupOldLiveUpdates() {
        try {
            if (liveUpdatesRef != null) {
                liveUpdatesRef.orderByKey().limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            child.getRef().removeValueAsync();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        log.warn("🔥 Failed to cleanup old live updates: {}", databaseError.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("🔥 Error during live updates cleanup", e);
        }
    }

    /**
     * Send custom notification through WebSocket
     */
    public void sendNotification(String topic, Object data) {
        try {
            if (messagingTemplate != null) {
                Map<String, Object> message = new HashMap<>();
                message.put("type", "notification");
                message.put("data", data);
                message.put("timestamp", System.currentTimeMillis());
                
                messagingTemplate.convertAndSend("/topic/" + topic, message);
                log.debug("🔥 Sent notification to topic: {}", topic);
            }
        } catch (Exception e) {
            log.error("🔥 Failed to send notification to topic: {}", topic, e);
        }
    }
}
