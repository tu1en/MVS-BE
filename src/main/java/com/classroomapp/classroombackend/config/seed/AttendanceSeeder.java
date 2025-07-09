package com.classroomapp.classroombackend.config.seed;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceSeeder {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final LectureRepository lectureRepository;
    private final ClassroomRepository classroomRepository;

    public void seed() {
        if (attendanceSessionRepository.count() > 0) {
            System.out.println("✅ [AttendanceSeeder] Attendance data already seeded.");
            return;
        }

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
        List<User> students = new ArrayList<>(classroom.getStudents());

        if (lectures.isEmpty() || students.isEmpty()) {
            System.out.println("⚠️ [AttendanceSeeder] No lectures or students for classroom: " + classroom.getName() + ", skipping.");
            return;
        }

        // Seed attendance for the first 3 lectures for variety
        for (int i = 0; i < Math.min(3, lectures.size()); i++) {
            Lecture lecture = lectures.get(i);

            // Skip if session already exists
            if (attendanceSessionRepository.findByLectureId(lecture.getId()).isPresent()) {
                continue;
            }

            AttendanceSession session = new AttendanceSession();
            session.setLecture(lecture);
            session.setSessionDate(lecture.getLectureDate());
            session.setClassroom(classroom);
            // Simulate clock-in time to populate teaching history
            session.setTeacherClockInTime(lecture.getLectureDate().atStartOfDay().plusHours(8));
            attendanceSessionRepository.save(session);

            // Create attendance records for each student
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
        System.out.println("✅ [AttendanceSeeder] Seeded attendance for classroom: " + classroom.getName());
    }
} 