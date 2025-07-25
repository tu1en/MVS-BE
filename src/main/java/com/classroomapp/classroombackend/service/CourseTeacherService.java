package com.classroomapp.classroombackend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.entity.Course;
import com.classroomapp.classroombackend.entity.CourseTeacher;
import com.classroomapp.classroombackend.entity.enumeration.CourseTeacherStatus;
import com.classroomapp.classroombackend.entity.enumeration.TeacherRole;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.CourseTeacherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseTeacherService {

    private final CourseTeacherRepository courseTeacherRepository;
    private final CourseService courseService;

    @Transactional
    public CourseTeacher assignTeacherToCourse(Long courseId, Long teacherId,
                                               TeacherRole role, String notes) {
        Course course = courseService.findById(courseId);
        User teacher = new User();
        teacher.setId(teacherId); // Giả lập user từ ID

        // Validate teacher availability
        validateTeacherAvailability(teacherId);

        // Check if already assigned
        if (courseTeacherRepository.existsByCourseIdAndTeacherIdAndIsActiveTrue(courseId, teacherId)) {
            throw new BusinessLogicException("Giáo viên đã được phân công cho khóa học này");
        }

        CourseTeacher assignment = new CourseTeacher();
        assignment.setCourse(course);
        assignment.setTeacher(teacher);
        assignment.setRole(role);
        assignment.setNotes(notes);

        return courseTeacherRepository.save(assignment);
    }

    @Transactional
    public void acceptTeacherAssignment(Long assignmentId, Long teacherId) {
        CourseTeacher assignment = findById(assignmentId);

        if (!assignment.getTeacher().getId().equals(teacherId)) {
            throw new BusinessLogicException("Bạn không có quyền thực hiện thao tác này");
        }

        assignment.acceptAssignment();
        courseTeacherRepository.save(assignment);
    }

    @Transactional
    public void declineTeacherAssignment(Long assignmentId, Long teacherId) {
        CourseTeacher assignment = findById(assignmentId);

        if (!assignment.getTeacher().getId().equals(teacherId)) {
            throw new BusinessLogicException("Bạn không có quyền thực hiện thao tác này");
        }

        assignment.declineAssignment();
        courseTeacherRepository.save(assignment);
    }

    @Transactional
    public void removeTeacherFromCourse(Long courseId, Long userId, Long managerId) {
        CourseTeacher assignment = courseTeacherRepository
                .findByCourseIdAndTeacherId(courseId, userId)
                .orElseThrow(() -> new BusinessLogicException("Không tìm thấy phân công"));

        assignment.removeAssignment();
        courseTeacherRepository.save(assignment);
    }

    public List<CourseTeacher> findByCourse(Long courseId) {
        return courseTeacherRepository.findByCourseIdAndIsActiveTrue(courseId);
    }

    public List<CourseTeacher> findByTeacher(Long teacherId) {
        return courseTeacherRepository.findByTeacherIdAndIsActiveTrue(teacherId);
    }

    public List<CourseTeacher> getTeacherAssignments(Long teacherId, CourseTeacherStatus status) {
        return courseTeacherRepository.findByTeacherIdAndStatusOrderByAssignedAtDesc(teacherId, status);
    }

    public List<CourseTeacher> getPendingAssignments(Long teacherId) {
        return courseTeacherRepository.findByTeacherIdAndStatusAndIsActiveTrue(teacherId, CourseTeacherStatus.PENDING);
    }

    public CourseTeacher findById(Long id) {
        return courseTeacherRepository.findById(id)
                .orElseThrow(() -> new BusinessLogicException("Không tìm thấy phân công"));
    }

    private void validateTeacherAvailability(Long teacherId) {
        long activeCount = courseTeacherRepository.countActiveCoursesByTeacher(teacherId);

        // Giới hạn ví dụ: tối đa 5 khóa học active cho một giáo viên
        if (activeCount >= 5) {
            throw new BusinessLogicException("Giáo viên đã đạt số khóa học tối đa được phân công");
        }
    }
}
