package com.classroomapp.classroombackend.integration;

import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateSessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateSlotDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SlotDto;
import com.classroomapp.classroombackend.model.classroommanagement.Slot;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;
import com.classroomapp.classroombackend.service.classroommanagement.SessionService;
import com.classroomapp.classroombackend.service.classroommanagement.SlotService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test cho Classroom Management Module
 * Test toàn bộ workflow từ tạo classroom -> session -> slot
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class ClassroomManagementIntegrationTest {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SlotService slotService;

    private static Long createdClassroomId;
    private static Long createdSessionId;
    private static Long createdSlotId;

    @Test
    @Order(1)
    void testCreateClassroom() {
        // Given
        CreateClassroomDto createDto = new CreateClassroomDto();
        createDto.setClassroomName("Test Integration Classroom");
        createDto.setDescription("Integration test classroom");
        createDto.setTeacherId(1L);

        // When
        ClassroomDto result = classroomService.createClassroom(createDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Integration Classroom", result.getClassroomName());
        assertEquals("Integration test classroom", result.getDescription());
        assertEquals(1L, result.getTeacherId());

        createdClassroomId = result.getId();
        System.out.println("✅ Created classroom with ID: " + createdClassroomId);
    }

    @Test
    @Order(2)
    void testCreateSession() {
        // Given
        assertNotNull(createdClassroomId, "Classroom must be created first");
        
        CreateSessionDto createDto = new CreateSessionDto();
        createDto.setClassroomId(createdClassroomId);
        createDto.setSessionDate(LocalDate.now().plusDays(1));
        createDto.setDescription("Integration test session");

        // When
        SessionDto result = sessionService.createSession(createDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(createdClassroomId, result.getClassroomId());
        assertEquals(LocalDate.now().plusDays(1), result.getSessionDate());
        assertEquals("Integration test session", result.getDescription());

        createdSessionId = result.getId();
        System.out.println("✅ Created session with ID: " + createdSessionId);
    }

    @Test
    @Order(3)
    void testCreateSlot() {
        // Given
        assertNotNull(createdSessionId, "Session must be created first");
        
        CreateSlotDto createDto = new CreateSlotDto();
        createDto.setSlotName("Test Integration Slot");
        createDto.setSessionId(createdSessionId);
        createDto.setStartTime(LocalTime.of(9, 0));
        createDto.setEndTime(LocalTime.of(10, 0));
        createDto.setDescription("Integration test slot");
        createDto.setStatus(Slot.SlotStatus.PENDING);

        // When
        SlotDto result = slotService.createSlot(createDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Integration Slot", result.getSlotName());
        assertEquals(createdSessionId, result.getSessionId());
        assertEquals(LocalTime.of(9, 0), result.getStartTime());
        assertEquals(LocalTime.of(10, 0), result.getEndTime());
        assertEquals("Integration test slot", result.getDescription());
        assertEquals(Slot.SlotStatus.PENDING, result.getStatus());

        createdSlotId = result.getId();
        System.out.println("✅ Created slot with ID: " + createdSlotId);
    }

    @Test
    @Order(4)
    void testGetClassroomWithSessions() {
        // Given
        assertNotNull(createdClassroomId, "Classroom must be created first");

        // When
        ClassroomDto classroom = classroomService.getClassroomById(createdClassroomId);

        // Then
        assertNotNull(classroom);
        assertEquals(createdClassroomId, classroom.getId());
        assertEquals("Test Integration Classroom", classroom.getClassroomName());

        System.out.println("✅ Retrieved classroom: " + classroom.getClassroomName());
    }

    @Test
    @Order(5)
    void testGetSessionWithSlots() {
        // Given
        assertNotNull(createdSessionId, "Session must be created first");

        // When
        SessionDto session = sessionService.getSessionById(createdSessionId);

        // Then
        assertNotNull(session);
        assertEquals(createdSessionId, session.getId());
        assertEquals(LocalDate.now().plusDays(1), session.getSessionDate());

        System.out.println("✅ Retrieved session for date: " + session.getSessionDate());
    }

    @Test
    @Order(6)
    void testGetSlotDetails() {
        // Given
        assertNotNull(createdSlotId, "Slot must be created first");

        // When
        SlotDto slot = slotService.getSlotById(createdSlotId);

        // Then
        assertNotNull(slot);
        assertEquals(createdSlotId, slot.getId());
        assertEquals("Test Integration Slot", slot.getSlotName());
        assertEquals(LocalTime.of(9, 0), slot.getStartTime());
        assertEquals(LocalTime.of(10, 0), slot.getEndTime());

        System.out.println("✅ Retrieved slot: " + slot.getSlotName() + 
                          " (" + slot.getStartTime() + " - " + slot.getEndTime() + ")");
    }

    @Test
    @Order(7)
    void testCompleteWorkflow() {
        // Test complete workflow is working
        assertNotNull(createdClassroomId, "Classroom should be created");
        assertNotNull(createdSessionId, "Session should be created");
        assertNotNull(createdSlotId, "Slot should be created");

        System.out.println("🎉 Complete integration test workflow successful!");
        System.out.println("   - Classroom ID: " + createdClassroomId);
        System.out.println("   - Session ID: " + createdSessionId);
        System.out.println("   - Slot ID: " + createdSlotId);
    }
}
