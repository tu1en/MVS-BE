package com.classroomapp.classroombackend.config.seed;

import com.classroomapp.classroombackend.model.JobPosition;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobPositionSeeder {
    private final JobPositionRepository jobPositionRepository;

    public void seed() {
        if (jobPositionRepository.count() == 0) {
            jobPositionRepository.save(new JobPosition(null, "Giáo viên lớp 10", "Dạy Toán, Lý, Hoá cho học sinh lớp 10", "12-18 triệu", null, null));
            jobPositionRepository.save(new JobPosition(null, "Giáo viên lớp 11", "Dạy Toán, Lý, Hoá cho học sinh lớp 11", "13-20 triệu", null, null));
            jobPositionRepository.save(new JobPosition(null, "Giáo viên lớp 12", "Dạy Toán, Lý, Hoá cho học sinh lớp 12, luyện thi đại học", "15-25 triệu", null, null));
        }
    }
} 