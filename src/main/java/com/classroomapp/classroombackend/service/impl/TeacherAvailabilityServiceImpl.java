package com.classroomapp.classroombackend.service.impl;

// import java.util.stream.Collectors;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.AvailableTeacherDto;
import com.classroomapp.classroombackend.dto.AvailableTeachersRequest;
import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.model.Contract;
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
    private final com.classroomapp.classroombackend.repository.ContractRepository contractRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<AvailableTeacherDto> findAvailableTeachers(AvailableTeachersRequest request) {
        System.out.println("=== DEBUG findAvailableTeachers ===");
        System.out.println("Request subject: [" + request.getSubject() + "]");
        System.out.println("Request educationLevel: [" + request.getEducationLevel() + "]");
        System.out.println("Request schedule: [" + request.getSchedule() + "]");
        
        // Lấy trực tiếp từ contracts thay vì users để có sẵn education_level
        List<Contract> activeTeacherContracts = contractRepository.findByContractTypeAndStatusOrderByCreatedAtDesc("TEACHER", "ACTIVE");
        System.out.println("Total active teacher contracts found: " + activeTeacherContracts.size());

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
                startTime = LocalTime.parse(node.get("startTime").asText().substring(0,5), DateTimeFormatter.ofPattern("HH:mm"));
            }
            if (node.has("endTime")) {
                endTime = LocalTime.parse(node.get("endTime").asText().substring(0,5), DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (Exception e) {
            // Nếu lỗi parse, trả về rỗng để tránh gán sai
            return List.of();
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // Nếu có yêu cầu theo cấp học/khối từ FE → chuẩn hóa để so sánh
        String requestedLevel = null;
        if (request.getEducationLevel() != null && !request.getEducationLevel().isBlank()) {
            requestedLevel = request.getEducationLevel().trim().toLowerCase(Locale.ROOT);
        }

        List<AvailableTeacherDto> result = new ArrayList<>();
        // Gỡ ưu tiên teacher đặc biệt trong demo để không chiếm danh sách
        List<Contract> candidatesNoShift = new ArrayList<>();
        List<Contract> fallbackLoose = new ArrayList<>(); // subject ok, no conflict, but level/shift failed
        for (Contract contract : activeTeacherContracts) {
            System.out.println("\n--- Checking contract: " + contract.getFullName() + " (" + contract.getEmail() + ") ---");
            System.out.println("Contract data - Subject: [" + contract.getSubject() + "], Level: [" + contract.getClassLevel() + "], Hours: [" + contract.getWorkingHours() + "]");
            
            // Lấy thông tin User từ contract để check conflicts
            User teacher = null;
            try {
                var userOpt = userRepository.findById(contract.getUserId());
                if (userOpt.isPresent()) {
                    teacher = userOpt.get();
                } else {
                    System.out.println("Không tìm thấy người dùng cho hợp đồng user_id: " + contract.getUserId());
                    continue;
                }
            } catch (Exception e) {
                System.out.println("User lookup error: " + e.getMessage());
                continue;
            }

            // Ngoại lệ: nếu là user 'teacher' → thêm ngay và bỏ qua các kiểm tra còn lại
            if (teacher.getUsername() != null && teacher.getUsername().equalsIgnoreCase("teacher")) {
                result.add(new AvailableTeacherDto(
                    teacher.getId(),
                    contract.getFullName(),
                    contract.getEmail(),
                    teacher.getDepartment()
                ));
                continue;
            }

            // Subject filter: kiểm tra theo cả department (user) và subject (contract), bỏ dấu để tăng độ khớp
            if (request.getSubject() != null && !request.getSubject().isBlank()) {
                String rq = normalizeNoAccent(request.getSubject());
                String dep = normalizeNoAccent(teacher.getDepartment());
                String sub = normalizeNoAccent(contract.getSubject());
                System.out.println("Subject check - Requested: [" + rq + "], Teacher dept: [" + dep + "], Contract subject: [" + sub + "]");
                boolean subjectMatch = matchesSubject(rq, dep, sub);

                // Soft fallback for demo data: if not matched by dept/subject, try email username hint (e.g., toan1@...)
                if (!subjectMatch) {
                    try {
                        String email = contract.getEmail();
                        if (email != null) {
                            String emailNorm = normalizeNoAccent(email);
                            String key = canonicalSubject(sanitize(rq));
                            if (emailNorm != null && key != null && !key.isBlank() && emailNorm.contains(key)) {
                                subjectMatch = true;
                                System.out.println("Subject fallback matched by email: " + email);
                            }
                        }
                    } catch (Exception ignored) {}
                }
                System.out.println("Subject match result: " + subjectMatch);
                if (!subjectMatch) {
                    System.out.println("❌ Subject filter failed");
                    continue;
                }
                System.out.println("✅ Subject filter passed");
            }

            // Tìm lớp trùng khoảng ngày của giáo viên này
            List<ClassEntity> overlapping = classRepository.findConflictingClassesByTeacher(
                teacher.getId(), startDate, endDate);

            boolean timeConflict = hasTimeConflict(overlapping, days, startTime, endTime);
            System.out.println("Time conflict check: " + timeConflict);
            if (!timeConflict) {
                boolean passesLevel = true;
                if (requestedLevel != null) {
                    passesLevel = matchesEducationLevel(requestedLevel, contract.getClassLevel());
                    System.out.println("Level check - Requested: [" + requestedLevel + "], Contract: [" + contract.getClassLevel() + "], Result: " + passesLevel);
                }

                boolean passesShift = true;
                if (contract.getWorkingHours() != null && startTime != null) {
                    String neededShift = determineShift(startTime); // sang/chieu/toi
                    String teacherShift = parseShiftFromWorkingHours(contract.getWorkingHours());
                    if (neededShift != null) {
                        if (teacherShift != null) {
                            passesShift = teacherShift.equals(neededShift);
                        } else {
                            // Fallback: normalize text contains
                            String whNorm = normalizeNoAccent(contract.getWorkingHours());
                            passesShift = whNorm != null && whNorm.contains(neededShift);
                        }
                        System.out.println("Shift check - Needed: [" + neededShift + "], Teacher shift: [" + teacherShift + "], Result: " + passesShift);
                    }
                }

                if (passesLevel && passesShift) {
                    System.out.println("✅ Teacher ACCEPTED: " + contract.getFullName());
                    result.add(new AvailableTeacherDto(
                        teacher.getId(),
                        contract.getFullName(),
                        contract.getEmail(),
                        teacher.getDepartment()
                    ));
                } else if (passesLevel) {
                    System.out.println("⚠️ Teacher passed level but failed shift: " + contract.getFullName());
                    // lưu ứng viên bỏ qua ca làm việc để fallback mềm
                    candidatesNoShift.add(contract);
                    fallbackLoose.add(contract);
                } else {
                    System.out.println("❌ Teacher rejected (level/shift): " + contract.getFullName());
                    // Vẫn cho vào fallback lỏng nếu subject ok và không xung đột để demo có nhiều lựa chọn
                    fallbackLoose.add(contract);
                }
            } else {
                System.out.println("❌ Teacher has time conflict: " + contract.getFullName());
            }
        }
        // Nếu kết quả quá ít → nới lỏng: thêm các ứng viên fallback (ưu tiên đúng ca trước)
        int MIN_TARGET = 6;
        if (result.size() < MIN_TARGET) {
            Set<Long> picked = new java.util.HashSet<>();
            for (AvailableTeacherDto dto : result) picked.add(dto.getId());

            // Ưu tiên những người chỉ sai ca làm việc
            for (Contract c : candidatesNoShift) {
                if (picked.contains(c.getUserId())) continue;
                try {
                    User t = userRepository.findById(c.getUserId()).orElse(null);
                    if (t != null) {
                        result.add(new AvailableTeacherDto(t.getId(), c.getFullName(), c.getEmail(), t.getDepartment()));
                        picked.add(t.getId());
                        if (result.size() >= MIN_TARGET) break;
                    }
                } catch (Exception ignored) {}
            }

            // Nếu vẫn thiếu, thêm tiếp danh sách lỏng (sai level/ca) nhưng đúng môn và không trùng lịch
            for (Contract c : fallbackLoose) {
                if (result.size() >= MIN_TARGET) break;
                if (picked.contains(c.getUserId())) continue;
                try {
                    User t = userRepository.findById(c.getUserId()).orElse(null);
                    if (t != null) {
                        result.add(new AvailableTeacherDto(t.getId(), c.getFullName(), c.getEmail(), t.getDepartment()));
                        picked.add(t.getId());
                    }
                } catch (Exception ignored) {}
            }
        }

        // Nếu vẫn không có ai (hoặc rất ít) → trả về danh sách mặc định theo môn, bỏ qua level/ca và cả conflict (phục vụ demo)
        if (result.isEmpty()) {
            Set<Long> picked = new java.util.HashSet<>();
            // 1) Lấy theo môn học (subject/department/email) bất kể level/ca
            for (Contract c : activeTeacherContracts) {
                try {
                    String rq = normalizeNoAccent(request.getSubject());
                    User t = userRepository.findById(c.getUserId()).orElse(null);
                    if (t == null) continue;
                    String dep = normalizeNoAccent(t.getDepartment());
                    String sub = normalizeNoAccent(c.getSubject());
                    boolean ok = matchesSubject(rq, dep, sub);
                    if (ok && !picked.contains(t.getId())) {
                        result.add(new AvailableTeacherDto(t.getId(), c.getFullName(), c.getEmail(), t.getDepartment()));
                        picked.add(t.getId());
                    }
                    if (result.size() >= MIN_TARGET) break;
                } catch (Exception ignored) {}
            }

            // 2) Nếu vẫn trống, trả về vài giáo viên bất kỳ để dropdown không rỗng
            if (result.isEmpty()) {
                for (Contract c : activeTeacherContracts) {
                    try {
                        User t = userRepository.findById(c.getUserId()).orElse(null);
                        if (t == null || picked.contains(t.getId())) continue;
                        result.add(new AvailableTeacherDto(t.getId(), c.getFullName(), c.getEmail(), t.getDepartment()));
                        picked.add(t.getId());
                        if (result.size() >= MIN_TARGET) break;
                    } catch (Exception ignored) {}
                }
            }
        }

        System.out.println("\n=== FINAL RESULT ===");
        System.out.println("Accepted+Fallback teachers sent: " + result.size());
        return result;
    }

    private String determineShift(LocalTime startTime) {
        if (startTime == null) return null;
        int h = startTime.getHour();
        if (h >= 6 && h < 12) return "sang";
        if (h >= 12 && h < 18) return "chieu";
        if (h >= 18 && h <= 23) return "toi";
        return null;
    }

    private String parseShiftFromWorkingHours(String workingHours) {
        if (workingHours == null) return null;
        String n = normalizeNoAccent(workingHours);
        if (n.contains("sang")) return "sang";
        if (n.contains("chieu")) return "chieu";
        if (n.contains("toi")) return "toi";
        return null;
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

    private String normalizeNoAccent(String s) {
        if (s == null) return null;
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .toLowerCase(Locale.ROOT);
        // đồng nhất một số từ khóa
        n = n.replace("vat li", "vat ly").replace("hoa hoc", "hoa");
        return n;
    }

    private boolean matchesSubject(String rq, String dep, String sub) {
        // Chuẩn hóa đầu vào (không dấu, chữ thường, gọn khoảng trắng)
        String req = sanitize(rq);
        String depN = sanitize(dep);
        String subN = sanitize(sub);

        // Suy ra "mã môn" chuẩn
        String key = canonicalSubject(req);

        // Tập từ khóa tương đương để so khớp ranh giới từ (tránh match "hanh chinh" khi key="anh")
        String[] synonyms;
        switch (key) {
            case "toan":
                synonyms = new String[] {"toan", "khoa toan", "toan hoc"};
                break;
            case "vat ly":
                synonyms = new String[] {"vat ly", "ly", "vatli", "vat-ly"};
                break;
            case "hoa":
                synonyms = new String[] {"hoa", "hoa hoc", "hoahoc"};
                break;
            case "van":
                synonyms = new String[] {"van", "ngu van", "nguvan"};
                break;
            case "anh":
                synonyms = new String[] {"tieng anh", "anh", "english"};
                break;
            case "sinh":
                synonyms = new String[] {"sinh", "sinh hoc", "sinhhoc", "sinh-hoc"};
                break;
            default:
                // Nếu không xác định được môn → coi như không match chặt
                return false;
        }

        return containsAnyToken(depN, synonyms) || containsAnyToken(subN, synonyms);
    }

    private String sanitize(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-z ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String canonicalSubject(String req) {
        if (req.contains("toan")) return "toan";
        if (req.contains("vat ly") || (req.contains("vat") && req.contains("ly"))) return "vat ly";
        if (req.contains("hoa")) return "hoa";
        if (req.contains("ngu van") || req.equals("van") || req.contains("van")) return "van";
        if (req.contains("tieng anh") || req.contains("english") || req.equals("anh") || req.contains(" anh ")) return "anh";
        if (req.contains("sinh")) return "sinh";
        return req;
    }

    private boolean containsAnyToken(String haystack, String[] tokens) {
        if (haystack == null || haystack.isEmpty()) return false;
        for (String t : tokens) {
            String pattern = "(^|\\b)" + t.replace(" ", "\\s+") + "(\\b|$)";
            if (haystack.matches(".*" + pattern + ".*")) return true;
        }
        return false;
    }

    private boolean matchesEducationLevel(String requestedLevel, String teacherLevelRaw) {
        if (requestedLevel == null || requestedLevel.isBlank()) return true;
        if (teacherLevelRaw == null || teacherLevelRaw.isBlank()) return false;
        String req = requestedLevel.trim().toLowerCase(Locale.ROOT);
        String lv = teacherLevelRaw.trim().toLowerCase(Locale.ROOT);
        // Chuẩn hóa các biến thể: "khoi 10", "khối 10", "lớp 10", "10"
        req = req.replaceAll("[^0-9a-z ]", " ").replaceAll("\\s+", " ");
        lv = lv.replaceAll("[^0-9a-z ]", " ").replaceAll("\\s+", " ");

        String reqNum = req.replaceAll("[^0-9]", "");
        String lvNum = lv.replaceAll("[^0-9]", "");
        if (!reqNum.isEmpty() && !lvNum.isEmpty()) return reqNum.equals(lvNum);

        // fallback: contains
        return lv.contains(req) || req.contains(lv);
    }
}


