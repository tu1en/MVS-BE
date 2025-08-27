package com.classroomapp.classroombackend.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.CreateEventDto;
import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.entity.LessonTemplate;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ClassScheduleSyncService {
    private static final Logger log = LoggerFactory.getLogger(ClassScheduleSyncService.class);

    @Autowired private ClassRepository classRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private TimetableService timetableService;
    @Autowired private TimetableEventRepository timetableEventRepository;
    @Autowired private com.classroomapp.classroombackend.repository.LessonTemplateRepository lessonTemplateRepository;
    @Autowired private ObjectMapper objectMapper;

    @Async
    public void generateTimetableForClassAsync(Long classId) {
        try {
            ClassEntity classEntity = classRepository.findById(classId).orElse(null);
            if (classEntity == null) return;

            Classroom classroom = findOrCreateClassroomForClass(classEntity);
            if (classroom == null) return;

            LocalTime parsedStart = null;
            LocalTime parsedEnd = null;
            List<Integer> dayIndexes = new ArrayList<>();

            if (classEntity.getScheduleJson() != null && !classEntity.getScheduleJson().isBlank()) {
                try {
                    JsonNode node = objectMapper.readTree(classEntity.getScheduleJson());
                    if (node.has("startTime")) parsedStart = LocalTime.parse(node.get("startTime").asText());
                    if (node.has("endTime")) parsedEnd = LocalTime.parse(node.get("endTime").asText());
                    if (node.has("days") && node.get("days").isArray()) {
                        for (JsonNode d : node.get("days")) dayIndexes.add(mapDayStringToIndex(d.asText()));
                    }
                } catch (Exception ignore) {}
            }

            if (dayIndexes.isEmpty()) dayIndexes.add(0); // Monday
            LocalTime st = parsedStart != null ? parsedStart : LocalTime.of(7, 30);
            LocalTime en = parsedEnd != null ? parsedEnd : st.plusMinutes(120);

            LocalDate startDate = classEntity.getStartDate();
            LocalDate endDate = classEntity.getEndDate();
            // Tránh LazyInitializationException trong thread async: load qua repository
            List<LessonTemplate> lessonTemplates = lessonTemplateRepository
                .findByCourseTemplateIdOrderByWeekNumberAscSortOrderAsc(
                    classEntity.getCourseTemplate().getId());

            // Determine createdBy for timetable events (use teacher if available)
            Long createdById = (classEntity.getTeacher() != null && classEntity.getTeacher().getId() != null)
                ? classEntity.getTeacher().getId()
                : (classEntity.getCreatedBy() != null ? classEntity.getCreatedBy() : null);

            if (lessonTemplates != null && !lessonTemplates.isEmpty()) {
                for (LessonTemplate lt : lessonTemplates) {
                    int week = lt.getWeekNumber() != null ? lt.getWeekNumber() : 1;
                    LocalDate targetDate = startDate.plusWeeks(Math.max(0, week - 1));
                    int desired = dayIndexes.get(0);
                    int current = targetDate.getDayOfWeek().getValue() - 1;
                    int delta = desired - current; if (delta < 0) delta += 7;
                    targetDate = targetDate.plusDays(delta);

                    CreateEventDto dto = new CreateEventDto();
                    dto.setTitle(lt.getTopicName());
                    dto.setDescription(lt.getObjectives());
                    dto.setStartDatetime(targetDate.atTime(st));
                    dto.setEndDatetime(targetDate.atTime(en));
                    dto.setEventType("CLASS");
                    dto.setClassroomId(classroom.getId());
                    dto.setLocation(classEntity.getRoom() != null ? classEntity.getRoom().getRoomName() : null);
                    dto.setIsAllDay(false);
                    dto.setColor("#007bff");

                    try {
                        // 🔍 ENHANCED: Check for existing events before creating to prevent conflicts
                        Long teacherId = createdById != null ? createdById : classroom.getTeacher() != null ? classroom.getTeacher().getId() : 1L;

                        // Check if event already exists
                        boolean eventExists = timetableEventRepository.findAll().stream()
                            .anyMatch(e -> e.getClassroomId().equals(classroom.getId()) &&
                                         e.getStartDatetime().equals(dto.getStartDatetime()) &&
                                         e.getEndDatetime().equals(dto.getEndDatetime()));

                        if (eventExists) {
                            log.debug("⏭️ Skipping duplicate event creation for class {} at {}", classId, dto.getStartDatetime());
                            continue;
                        }

                        // Check for teacher conflicts
                        boolean hasConflict = timetableEventRepository.findAll().stream()
                            .anyMatch(e -> e.getCreatedBy().equals(teacherId) &&
                                         e.getStartDatetime().isBefore(dto.getEndDatetime()) &&
                                         dto.getStartDatetime().isBefore(e.getEndDatetime()));

                        if (hasConflict) {
                            log.warn("⚠️ Teacher conflict detected for class {} at {}, skipping event creation", classId, dto.getStartDatetime());
                            continue;
                        }

                        timetableService.createEvent(dto, teacherId);
                    } catch (Exception e) {
                        log.warn("Background createEvent failed for class {} week {}: {}", classId, week, e.getMessage());
                    }
                }
            } else if (endDate != null) {
                // Fallback: không có LessonTemplate ⇒ tạo chuỗi sự kiện hàng tuần theo khoảng ngày
                LocalDate first = startDate;
                int desired = dayIndexes.get(0);
                int current = first.getDayOfWeek().getValue() - 1;
                int delta = desired - current; if (delta < 0) delta += 7;
                first = first.plusDays(delta);
                for (LocalDate d = first; !d.isAfter(endDate); d = d.plusWeeks(1)) {
                    CreateEventDto dto = new CreateEventDto();
                    dto.setTitle(classEntity.getClassName());
                    dto.setDescription(classEntity.getDescription());
                    dto.setStartDatetime(d.atTime(st));
                    dto.setEndDatetime(d.atTime(en));
                    dto.setEventType("CLASS");
                    dto.setClassroomId(classroom.getId());
                    dto.setLocation(classEntity.getRoom() != null ? classEntity.getRoom().getRoomName() : null);
                    dto.setIsAllDay(false);
                    dto.setColor("#007bff");
                    try {
                        timetableService.createEvent(dto, createdById != null ? createdById : classroom.getTeacher() != null ? classroom.getTeacher().getId() : 1L);
                    } catch (Exception e) {
                        log.warn("Background createEvent failed for class {} date {}: {}", classId, d, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("generateTimetableForClassAsync error: {}", e.getMessage());
        }
    }

    private Classroom findOrCreateClassroomForClass(ClassEntity classEntity) {
        List<Classroom> existing = classroomRepository.findAll();
        Classroom found = existing.stream()
            .filter(c -> c.getName() != null && c.getName().equals(classEntity.getClassName()))
            .findFirst().orElse(null);
        if (found != null) return found;

        Classroom n = new Classroom();
        n.setName(classEntity.getClassName());
        n.setDescription(classEntity.getDescription());
        if (classEntity.getCourseTemplate() != null) n.setSubject(classEntity.getCourseTemplate().getSubject());
        if (classEntity.getTeacher() != null) n.setTeacher(classEntity.getTeacher());
        return classroomRepository.save(n);
    }

    private int mapDayStringToIndex(String day) {
        if (day == null) return 0;
        String d = day.trim().toLowerCase();
        switch (d) {
            case "mon": case "monday": case "thu2": return 0;
            case "tue": case "tuesday": case "thu3": return 1;
            case "wed": case "wednesday": case "thu4": return 2;
            case "thu": case "thursday": case "thu5": return 3;
            case "fri": case "friday": case "thu6": return 4;
            case "sat": case "saturday": case "thu7": return 5;
            case "sun": case "sunday": case "cn": return 6;
            default: return 0;
        }
    }
}


