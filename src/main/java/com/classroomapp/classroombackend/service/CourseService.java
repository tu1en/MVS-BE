package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.CourseDetailResponse;
import org.springframework.stereotype.Service;

@Service
public class CourseService {
    public CourseDetailResponse getCourseDetail(Long id) {
        // TODO: Replace with DB fetch. Demo data:
        if (id == 1L) {
            return new CourseDetailResponse(1L, "Toán Cao Cấp", "MATH101", "Nguyễn Văn A", "Thứ 2, 4, 6 (7:00-9:00)", "Giải tích, Đại số, Hình học");
        } else if (id == 2L) {
            return new CourseDetailResponse(2L, "Lập Trình Cơ Bản", "CS100", "Trần Thị B", "Thứ 3, 5 (8:00-10:00)", "Cơ bản C/C++, giải thuật");
        }
        return new CourseDetailResponse(id, "Khoá học mẫu", "CODE", "GV Demo", "Lịch học mẫu", "Syllabus mẫu");
    }
}
