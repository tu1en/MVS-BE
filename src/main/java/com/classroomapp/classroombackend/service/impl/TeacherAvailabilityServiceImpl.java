package com.classroomapp.classroombackend.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.AvailableTeacherDto;
import com.classroomapp.classroombackend.dto.AvailableTeachersRequest;
import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeacherAvailabilityServiceImpl implements com.classroomapp.classroombackend.service.TeacherAvailabilityService {

    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<AvailableTeacherDto> findAvailableTeachers(AvailableTeachersRequest request) {
        List<User> teachers = userRepository.findByRoleId(2);

        // Lọc theo môn nếu có (tạm thời dựa vào department của user cho field môn)
        if (request.getSubject() != null && !request.getSubject().isBlank()) {
            String subjectLower = request.getSubject().toLowerCase(Locale.ROOT);
            teachers = teachers.stream()
                .filter(u -> u.getDepartment() != null && u.getDepartment().toLowerCase(Locale.ROOT).contains(subjectLower))
                .collect(Collectors.toList());
        }

        // Parse schedule JSON
        Set<Integer> days = new HashSet<>();
        LocalTime startTime = null;
        LocalTime endTime = null;
        try {
            JsonNode node = objectMapper.readTree(request.getSchedule());
            if (node.has("days")) {
                for (JsonNode d : node.get("days")) {
                    String val = d.asText();
                    days.add(mapDayStringToIndex(val));
                }
            }
            if (node.has("startTime")) {
                startTime = LocalTime.parse(node.get("startTime").asText(), DateTimeFormatter.ofPattern("HH:mm"));
            }
            if (node.has("endTime")) {
                endTime = LocalTime.parse(node.get("endTime").asText(), DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (Exception e) {
            // Nếu lỗi parse, trả về rỗng để tránh gán sai
            return List.of();
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<AvailableTeacherDto> result = new ArrayList<>();
        for (User teacher : teachers) {
            // Tìm lớp trùng khoảng ngày của giáo viên này
            List<ClassEntity> overlapping = classRepository.findConflictingClassesByTeacher(
                teacher.getId(), startDate, endDate);

            boolean timeConflict = hasTimeConflict(overlapping, days, startTime, endTime);
            if (!timeConflict) {
                result.add(new AvailableTeacherDto(
                    teacher.getId(),
                    teacher.getFullName(),
                    teacher.getEmail(),
                    teacher.getDepartment()
                ));
            }
        }
        return result;
    }

    private boolean hasTimeConflict(List<ClassEntity> classes, Set<Integer> targetDays, LocalTime targetStart, LocalTime targetEnd) {
        if (classes == null || classes.isEmpty()) return false;
        for (ClassEntity c : classes) {
            String json = c.getScheduleJson();
            if (json == null || json.isBlank()) continue;
            try {
                JsonNode node = objectMapper.readTree(json);
                Set<Integer> classDays = new HashSet<>();
                if (node.has("days")) {
                    for (JsonNode d : node.get("days")) {
                        classDays.add(mapDayStringToIndex(d.asText()));
                    }
                }
                LocalTime classStart = node.has("startTime") ? LocalTime.parse(node.get("startTime").asText(), DateTimeFormatter.ofPattern("HH:mm")) : null;
                LocalTime classEnd = node.has("endTime") ? LocalTime.parse(node.get("endTime").asText(), DateTimeFormatter.ofPattern("HH:mm")) : null;

                // Nếu có giao ngày trong tuần và chồng lấn giờ → conflict
                boolean dayOverlap = !disjoint(classDays, targetDays);
                boolean timeOverlap = classStart != null && classEnd != null && targetStart != null && targetEnd != null &&
                    !(classEnd.isBefore(targetStart) || classStart.isAfter(targetEnd));
                if (dayOverlap && timeOverlap) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private static boolean disjoint(Set<Integer> a, Set<Integer> b) {
        for (Integer x : a) if (b.contains(x)) return false;
        return true;
    }

    private int mapDayStringToIndex(String day) {
        if (day == null) return -1;
        String d = day.trim().toUpperCase(Locale.ROOT);
        switch (d) {
            case "MON": case "MONDAY": case "THU 2": return DayOfWeek.MONDAY.getValue() % 7; // 1 -> 1, but our Schedule uses 0=Mon
            case "TUE": case "TUESDAY": case "THU 3": return DayOfWeek.TUESDAY.getValue() % 7;
            case "WED": case "WEDNESDAY": case "THU 4": return DayOfWeek.WEDNESDAY.getValue() % 7;
            case "THU": case "THURSDAY": case "THU 5": return DayOfWeek.THURSDAY.getValue() % 7;
            case "FRI": case "FRIDAY": case "THU 6": return DayOfWeek.FRIDAY.getValue() % 7;
            case "SAT": case "SATURDAY": case "THU 7": return DayOfWeek.SATURDAY.getValue() % 7;
            case "SUN": case "SUNDAY": case "CHU NHAT": return 0; // map to Sunday index 0 here
            default: return -1;
        }
    }
}


