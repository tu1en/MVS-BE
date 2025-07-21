package com.classroomapp.classroombackend.config.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.classroommanagement.Course;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseSeeder {

    @Autowired
    private CourseRepository courseRepository;

    public void seed() {
        try {
            System.out.println("🔍 [CourseSeeder] Starting course seeding...");
            long count = courseRepository.count();
            System.out.println("🔍 [CourseSeeder] Current course count: " + count);

            if (count == 0) {
                System.out.println("🔍 [CourseSeeder] Creating courses...");

                Course math = new Course();
                math.setName("Toán học nâng cao");
                math.setDescription("Nghiên cứu toàn diện các khái niệm toán học và ứng dụng của chúng.");
                courseRepository.save(math);
                System.out.println("✅ [CourseSeeder] Created: " + math.getName());

                Course history = new Course();
                history.setName("Lịch sử thế giới");
                history.setDescription("Khảo sát các sự kiện lịch sử quan trọng từ các nền văn minh cổ đại đến thời hiện đại.");
                courseRepository.save(history);
                System.out.println("✅ [CourseSeeder] Created: " + history.getName());

                Course literature = new Course();
                literature.setName("Văn học Việt Nam");
                literature.setDescription("Khám phá các tác phẩm văn học Việt Nam qua các thời kỳ lịch sử.");
                courseRepository.save(literature);
                System.out.println("✅ [CourseSeeder] Created: " + literature.getName());

                Course english = new Course();
                english.setName("Tiếng Anh giao tiếp");
                english.setDescription("Phát triển kỹ năng giao tiếp tiếng Anh trong môi trường quốc tế.");
                courseRepository.save(english);
                System.out.println("✅ [CourseSeeder] Created: " + english.getName());

                Course cs = new Course();
                cs.setName("Khoa học máy tính");
                cs.setDescription("Các khái niệm cơ bản về khoa học máy tính và lập trình.");
                courseRepository.save(cs);
                System.out.println("✅ [CourseSeeder] Created: " + cs.getName());

                Course physics = new Course();
                physics.setName("Vật lý đại cương");
                physics.setDescription("Giới thiệu các nguyên lý cơ bản của vật lý.");
                courseRepository.save(physics);
                System.out.println("✅ [CourseSeeder] Created: " + physics.getName());

                System.out.println("✅ [CourseSeeder] Created 6 sample courses.");
            } else {
                System.out.println("ℹ️ [CourseSeeder] Courses already exist, skipping seeding.");
            }
        } catch (Exception e) {
            System.err.println("❌ [CourseSeeder] Error during seeding: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 