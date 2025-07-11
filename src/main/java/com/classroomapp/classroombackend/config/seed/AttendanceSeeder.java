package com.classroomapp.classroombackend.config.seed;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceSeeder {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final LectureRepository lectureRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;

    @Transactional
    public void seed() {
        // We will now always run this seeder to ensure some teaching history exists.
        System.out.println("🔄 [AttendanceSeeder] Seeding attendance data and ensuring teaching history...");

        List<Classroom> classrooms = classroomRepository.findAll();
        if (classrooms.isEmpty()) {
            System.out.println("⚠️ [AttendanceSeeder] No classrooms found, skipping seed.");
            return;
        }

        for (Classroom classroom : classrooms) {
            seedAttendanceForClassroom(classroom);
        }

        System.out.println("✅ [AttendanceSeeder] Finished seeding attendance data.");
    }

    private void seedAttendanceForClassroom(Classroom classroom) {
        List<Lecture> lectures = lectureRepository.findByClassroomId(classroom.getId());
        List<User> students = classroomEnrollmentRepository.findByClassroomId(classroom.getId())
                .stream()
                .map(enrollment -> enrollment.getUser())
                .collect(Collectors.toList());

        if (lectures.isEmpty()) {
            System.out.println("⚠️ [AttendanceSeeder] No lectures for classroom: " + classroom.getName() + ", skipping.");
            return;
        }

        // Seed attendance for lectures that have a date in the past or today
        for (Lecture lecture : lectures) {
            // Check if lecture has a date and if it's in the past or today
            if (lecture.getLectureDate() != null && !lecture.getLectureDate().isAfter(LocalDate.now())) {
                
                // Find existing or create a new session
                AttendanceSession session = attendanceSessionRepository.findByLectureId(lecture.getId())
                    .orElse(new AttendanceSession());

                // Always ensure there is a clock-in time for past lectures
                if (session.getTeacherClockInTime() == null) {
                    session.setTeacherClockInTime(lecture.getLectureDate().atTime(8, 30)); // Set a fixed time for consistency
                }
                
                // If it's a new session, set its properties
                if (session.getId() == null) {
                    session.setLecture(lecture);
                    session.setSessionDate(lecture.getLectureDate());
                    session.setClassroom(classroom);
                    attendanceSessionRepository.save(session);

                    // Create attendance records for each student only for the new session
                    if (!students.isEmpty()) {
                        for (int j = 0; j < students.size(); j++) {
                            User student = students.get(j);
                            Attendance attendance = new Attendance();
                            attendance.setSession(session);
                            attendance.setStudent(student);
                            
                            // Alternate status for variety
                            attendance.setStatus(j % 3 == 0 ? AttendanceStatus.ABSENT : (j % 3 == 1 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT));
                            attendanceRepository.save(attendance);
                        }
                    }
                } else {
                    // If the session already exists, just save the updated clock-in time
                    attendanceSessionRepository.save(session);
                }
            }
        }
        System.out.println("✅ [AttendanceSeeder] Seeded/updated attendance for classroom: " + classroom.getName());
    }
} 