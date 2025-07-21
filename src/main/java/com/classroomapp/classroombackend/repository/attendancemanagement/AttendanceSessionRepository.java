package com.classroomapp.classroombackend.repository.attendancemanagement;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findByClassroomId(Long classroomId);
    Optional<AttendanceSession> findByLectureId(Long lectureId);
    
    // Method for LiveSessionAttendanceServiceImpl
    List<AttendanceSession> findByLectureAndIsActiveOrderByCreatedAtDesc(Lecture lecture, boolean isActive);
    
    boolean existsByClassroomIdAndStatus(Long classroomId, AttendanceSession.SessionStatus status);

    /**
     * Tìm các phiên điểm danh mà giáo viên đã clock-in
     * @param teacherId ID của giáo viên
     * @return Danh sách các phiên điểm danh
     */
    @Query("SELECT s FROM AttendanceSession s " +
           "WHERE s.teacher.id = :teacherId " +
           "AND s.teacherClockInTime IS NOT NULL " +
           "ORDER BY s.teacherClockInTime DESC")
    List<AttendanceSession> findTeachingHistoryByTeacherId(@Param("teacherId") Long teacherId);

    long countByClassroomIdIn(List<Long> classroomIds);
}
