package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.LectureMaterialRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;

@Component
@Transactional
public class LectureSeeder {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private LectureMaterialRepository lectureMaterialRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Transactional
    public void seed(List<Classroom> classrooms) {
        System.out.println("🔄 [LectureSeeder] Starting lecture seeding process...");

        if (classrooms.isEmpty()) {
            System.out.println("⚠️ [LectureSeeder] No classrooms provided. Skipping.");
            return;
        }

        System.out.println("📚 [LectureSeeder] Checking " + classrooms.size() + " classrooms for lectures...");

        for (Classroom classroom : classrooms) {
            if (lectureRepository.existsByClassroomId(classroom.getId())) {
                System.out.println("✅ [LectureSeeder] Classroom '" + classroom.getName() + "' already has lectures. Skipping.");
                continue;
            }

            System.out.println("🔧 [LectureSeeder] No lectures found for '" + classroom.getName() + "'. Creating now...");

            if (classroom.getName().contains("Toán")) {
                createMathLectures(classroom);
            } else {
                createSampleLecturesForClassroom(classroom);
            }
        }

        long totalLectures = lectureRepository.count();
        System.out.println("✅ [LectureSeeder] Seeding process completed. Total lectures in database: " + totalLectures);
    }

    private void createMathLectures(Classroom mathClass) {
        System.out.println("🧮 [LectureSeeder] Creating lectures for Math classroom: " + mathClass.getName());

        List<Schedule> schedules = scheduleRepository.findByClassroomId(mathClass.getId());
        Schedule scheduleToLink = schedules.isEmpty() ? null : schedules.get(0);

        if (scheduleToLink != null) {
            // ✅ Fixed: Use new field getTitle() instead of getSubject()
            System.out.println("✅ [LectureSeeder] Found schedule to link: " + 
                (scheduleToLink.getTitle() != null ? scheduleToLink.getTitle() : "Untitled Schedule"));
        } else {
            System.out.println("⚠️ [LectureSeeder] No schedule found for Math classroom, creating lectures without schedule link");
        }

        // Lecture 1
        Lecture mathLecture1 = new Lecture();
        mathLecture1.setTitle("Giới thiệu về Đạo hàm");
        mathLecture1.setContent("# Giới thiệu về Đạo hàm\n\n## Định nghĩa đạo hàm\n\nĐạo hàm của một hàm số f(x) tại điểm x₀, ký hiệu là f'(x₀), được định nghĩa là:\n\nf'(x₀) = lim(h→0) [f(x₀+h) - f(x₀)]/h\n\nĐạo hàm cho ta biết tốc độ biến thiên của hàm số tại một điểm.\n\n## Các quy tắc tính đạo hàm\n\n1. Đạo hàm của hằng số: (C)' = 0\n2. Đạo hàm của x^n: (x^n)' = n*x^(n-1)\n3. Đạo hàm của tổng, hiệu: (u ± v)' = u' ± v'\n4. Đạo hàm của tích: (uv)' = u'v + uv'\n5. Đạo hàm của thương: (u/v)' = (u'v - uv')/v²");
        mathLecture1.setClassroom(mathClass);
        mathLecture1.setLectureDate(LocalDate.of(2025, 7, 9));
        if (scheduleToLink != null) {
            mathLecture1.setSchedule(scheduleToLink);
        }
        lectureRepository.save(mathLecture1);
        System.out.println("✅ [LectureSeeder] Created lecture 1: " + mathLecture1.getTitle());

        // Lecture 2
        Lecture mathLecture2 = new Lecture();
        mathLecture2.setTitle("Tích phân và Ứng dụng");
        mathLecture2.setContent("# Tích phân và Ứng dụng\n\n## Nguyên hàm\n\nNếu F'(x) = f(x) thì F(x) được gọi là một nguyên hàm của f(x).\n\n## Tích phân xác định\n\nTích phân xác định của f(x) từ a đến b, ký hiệu là ∫[a,b] f(x)dx, cho ta diện tích hình thang cong giới hạn bởi đồ thị y=f(x), trục Ox và hai đường thẳng x=a, x=b.\n\n## Ứng dụng của tích phân\n\n1. Tính diện tích hình phẳng\n2. Tính thể tích vật thể tròn xoay\n3. Tính độ dài cung\n4. Ứng dụng trong vật lý và kỹ thuật");
        mathLecture2.setClassroom(mathClass);
        mathLecture2.setLectureDate(LocalDate.now().plusDays(2));
        if (scheduleToLink != null) {
            mathLecture2.setSchedule(scheduleToLink);
        }
        lectureRepository.save(mathLecture2);
        System.out.println("✅ [LectureSeeder] Created lecture 2: " + mathLecture2.getTitle());

        // Lecture 3
        Lecture mathLecture3 = new Lecture();
        mathLecture3.setTitle("Phương trình vi phân");
        mathLecture3.setContent("# Phương trình vi phân\n\n## Khái niệm cơ bản\n\nPhương trình vi phân là phương trình chứa hàm số chưa biết và các đạo hàm của nó.\n\n## Phương trình vi phân cấp 1\n\nDạng tổng quát: F(x, y, y') = 0\n\n### Phương trình tách biến\n\nDạng: dy/dx = f(x)g(y)\n\nCách giải: ∫dy/g(y) = ∫f(x)dx\n\n### Phương trình tuyến tính cấp 1\n\nDạng: y' + P(x)y = Q(x)\n\nCông thức nghiệm: y = e^(-∫P(x)dx)[∫Q(x)e^(∫P(x)dx)dx + C]");
        mathLecture3.setClassroom(mathClass);
        mathLecture3.setLectureDate(LocalDate.now().plusDays(7));
        if (scheduleToLink != null) {
            mathLecture3.setSchedule(scheduleToLink);
        }
        lectureRepository.save(mathLecture3);
        System.out.println("✅ [LectureSeeder] Created lecture 3: " + mathLecture3.getTitle());

        System.out.println("✅ [LectureSeeder] Successfully created 3 lectures for Math classroom");
    }

    private void createSampleLecturesForClassroom(Classroom classroom) {
        System.out.println("📖 [LectureSeeder] Creating sample lectures for: " + classroom.getName());

        List<Schedule> schedules = scheduleRepository.findByClassroomId(classroom.getId());
        Schedule scheduleToLink = schedules.isEmpty() ? null : schedules.get(0);

        // Lecture 1
        Lecture lecture1 = new Lecture();
        lecture1.setTitle("Bài giảng giới thiệu - " + classroom.getName());
        lecture1.setContent("# Bài giảng giới thiệu\n\n## Chào mừng đến với khóa học " + classroom.getName() + "\n\nĐây là bài giảng đầu tiên trong khóa học. Chúng ta sẽ tìm hiểu về:\n\n- Mục tiêu của khóa học\n- Nội dung chính\n- Phương pháp học tập\n- Đánh giá và kiểm tra\n\n## Yêu cầu\n\n- Tham gia đầy đủ các buổi học\n- Hoàn thành bài tập được giao\n- Tích cực tham gia thảo luận\n\nChúc các bạn học tập hiệu quả!");
        lecture1.setClassroom(classroom);
        lecture1.setLectureDate(LocalDate.now().minusDays(1));
        if (scheduleToLink != null) {
            lecture1.setSchedule(scheduleToLink);
        }
        lectureRepository.save(lecture1);

        // Lecture 2
        Lecture lecture2 = new Lecture();
        lecture2.setTitle("Bài học thực hành - " + classroom.getName());
        lecture2.setContent("# Bài học thực hành\n\n## Mục tiêu\n\nTrong bài học này, chúng ta sẽ:\n\n- Áp dụng kiến thức đã học\n- Thực hành qua các bài tập\n- Thảo luận và giải đáp thắc mắc\n\n## Nội dung thực hành\n\n1. Ôn tập kiến thức cơ bản\n2. Giải các bài tập mẫu\n3. Thực hành độc lập\n4. Thảo luận kết quả\n\n## Bài tập về nhà\n\nHoàn thành các bài tập được giao và chuẩn bị cho buổi học tiếp theo.");
        lecture2.setClassroom(classroom);
        lecture2.setLectureDate(LocalDate.now().plusDays(3));
        if (scheduleToLink != null) {
            lecture2.setSchedule(scheduleToLink);
        }
        lectureRepository.save(lecture2);

        System.out.println("✅ [LectureSeeder] Created 2 sample lectures for: " + classroom.getName());
    }

    @Transactional
    public void forceSeed(List<Classroom> classrooms) {
        System.out.println("🔄 [LectureSeeder] FORCE SEEDING - Clearing existing lectures...");
        lectureRepository.deleteAll();
        System.out.println("✅ [LectureSeeder] Cleared all existing lectures");

        System.out.println("📚 [LectureSeeder] Force seeding lectures for all " + classrooms.size() + " classrooms...");
        for (Classroom classroom : classrooms) {
            if (classroom.getName().contains("Toán")) {
                createMathLectures(classroom);
            } else {
                createSampleLecturesForClassroom(classroom);
            }
        }
    }

    public void verifyLecturesForClassroom(Long classroomId) {
        List<Lecture> lectures = lectureRepository.findByClassroomId(classroomId);
        System.out.println("🔍 [LectureSeeder] Verification for classroom " + classroomId + ": " + lectures.size() + " lectures found");

        if (lectures.isEmpty()) {
            System.out.println("❌ [LectureSeeder] WARNING: No lectures found for classroom " + classroomId);

            Classroom classroom = classroomRepository.findById(classroomId).orElse(null);
            if (classroom != null) {
                System.out.println("🔧 [LectureSeeder] Attempting to create lectures for classroom: " + classroom.getName());
                if (classroom.getName().contains("Toán")) {
                    createMathLectures(classroom);
                } else {
                    createSampleLecturesForClassroom(classroom);
                }
            }
        } else {
            for (Lecture lecture : lectures) {
                System.out.println("   - " + lecture.getTitle() + " (ID: " + lecture.getId() + ", Date: " + lecture.getLectureDate() + ")");
            }
        }
    }
}