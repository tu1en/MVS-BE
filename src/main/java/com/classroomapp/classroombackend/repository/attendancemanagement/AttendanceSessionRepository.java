package com.classroomapp.classroombackend.repository.attendancemanagement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * Tìm các phiên điểm danh theo danh sách classroom và khoảng thời gian teacherClockInTime
     * @param classroomIds Danh sách ID của các classroom
     * @param startTime Thời gian bắt đầu
     * @param endTime Thời gian kết thúc
     * @return Danh sách các phiên điểm danh
     */
    @Query("SELECT s FROM AttendanceSession s " +
           "WHERE s.classroom.id IN :classroomIds " +
           "AND s.teacherClockInTime BETWEEN :startTime AND :endTime " +
           "ORDER BY s.teacherClockInTime ASC")
    List<AttendanceSession> findByClassroomIdInAndTeacherClockInTimeBetween(
            @Param("classroomIds") List<Long> classroomIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    long countByClassroomIdIn(List<Long> classroomIds);
}
