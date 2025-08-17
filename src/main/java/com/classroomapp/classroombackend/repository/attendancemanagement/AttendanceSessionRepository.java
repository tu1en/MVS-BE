package com.classroomapp.classroombackend.repository.attendancemanagement;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findByClassroomId(Long classroomId);
    Optional<AttendanceSession> findTopByLectureIdOrderByCreatedAtDesc(Long lectureId);

    
    Optional<AttendanceSession> findByClassroomIdAndIsOpenTrue(Long classroomId);

    
    List<AttendanceSession> findByClassroom_TeacherId(Long teacherId);
    
    boolean existsByClassroomIdAndStatus(Long classroomId, AttendanceSession.SessionStatus status);

    /**
     * Tìm các phiên điểm danh mà giáo viên đã clock-in
     * @param teacherId ID của giáo viên
     * @return Danh sách các phiên điểm danh
     */
    @Query("SELECT s FROM AttendanceSession s " +
           "JOIN s.lecture l " +
           "JOIN s.classroom c " +
           "WHERE c.teacher.id = :teacherId " +
           "AND s.teacherClockInTime IS NOT NULL " +
           "ORDER BY s.teacherClockInTime DESC")
    List<AttendanceSession> findTeachingHistoryByTeacherId(@Param("teacherId") Long teacherId);

    long countByClassroomIdIn(List<Long> classroomIds);
}
