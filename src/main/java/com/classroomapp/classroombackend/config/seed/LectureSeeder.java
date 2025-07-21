package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.LectureMaterial;
import com.classroomapp.classroombackend.model.LectureRecording;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.LectureMaterialRepository;
import com.classroomapp.classroombackend.repository.LectureRecordingRepository;
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

    @Autowired
    private LectureRecordingRepository lectureRecordingRepository;

    private final Random random = new Random();

    @Transactional
    public void seed(List<Classroom> classrooms) {
        System.out.println("🔄 [LectureSeeder] Starting lecture seeding process...");

        if (classrooms.isEmpty()) {
            System.out.println("⚠️ [LectureSeeder] No classrooms provided. Skipping.");
            return;
        }

        System.out.println("📚 [LectureSeeder] Found " + classrooms.size() + " classrooms to process");

        int lecturesCreated = 0;
        for (Classroom classroom : classrooms) {
            if (lectureRepository.existsByClassroomId(classroom.getId())) {
                System.out.println("✅ [LectureSeeder] Classroom '" + classroom.getName() + "' already has lectures. Skipping.");
                continue;
            }

            System.out.println("🔄 [LectureSeeder] Creating lectures for classroom: " + classroom.getName());
            
            if (classroom.getName().contains("Toán")) {
                createMathLectures(classroom);
                lecturesCreated += 3; // Math has 3 lectures
            } else {
                createSampleLecturesForClassroom(classroom);
                lecturesCreated += 2; // Regular has 2 lectures
            }
            
            System.out.println("✅ [LectureSeeder] Completed lectures for classroom: " + classroom.getName());
        }

        System.out.println("📚 [LectureSeeder] Created " + lecturesCreated + " lectures in total");
        
        seedLectureMaterials();
        seedLectureRecordings();
        
        System.out.println("🎉 [LectureSeeder] Lecture seeding process completed successfully!");
    }

    private void createMathLectures(Classroom mathClass) {
        List<Schedule> schedules = scheduleRepository.findByClassroomId(mathClass.getId());
        Schedule scheduleToLink = schedules.isEmpty() ? null : schedules.get(0);

        Lecture mathLecture1 = new Lecture();
        mathLecture1.setTitle("Giới thiệu về Đạo hàm");
        mathLecture1.setContent("## Định nghĩa đạo hàm...");
        mathLecture1.setClassroom(mathClass);
        mathLecture1.setLectureDate(LocalDate.of(2025, 7, 9));
        if (scheduleToLink != null) mathLecture1.setSchedule(scheduleToLink);
        lectureRepository.save(mathLecture1);

        Lecture mathLecture2 = new Lecture();
        mathLecture2.setTitle("Tích phân và Ứng dụng");
        mathLecture2.setContent("## Nguyên hàm...");
        mathLecture2.setClassroom(mathClass);
        mathLecture2.setLectureDate(LocalDate.now().plusDays(2));
        if (scheduleToLink != null) mathLecture2.setSchedule(scheduleToLink);
        lectureRepository.save(mathLecture2);

        Lecture mathLecture3 = new Lecture();
        mathLecture3.setTitle("Phương trình vi phân");
        mathLecture3.setContent("## Phương trình vi phân...");
        mathLecture3.setClassroom(mathClass);
        mathLecture3.setLectureDate(LocalDate.now().plusDays(7));
        if (scheduleToLink != null) mathLecture3.setSchedule(scheduleToLink);
        lectureRepository.save(mathLecture3);
    }

    private void createSampleLecturesForClassroom(Classroom classroom) {
        List<Schedule> schedules = scheduleRepository.findByClassroomId(classroom.getId());
        Schedule scheduleToLink = schedules.isEmpty() ? null : schedules.get(0);

        Lecture lecture1 = new Lecture();
        lecture1.setTitle("Bài giảng giới thiệu - " + classroom.getName());
        lecture1.setContent("## Giới thiệu...");
        lecture1.setClassroom(classroom);
        lecture1.setLectureDate(LocalDate.now().minusDays(1));
        if (scheduleToLink != null) lecture1.setSchedule(scheduleToLink);
        lectureRepository.save(lecture1);

        Lecture lecture2 = new Lecture();
        lecture2.setTitle("Bài học thực hành - " + classroom.getName());
        lecture2.setContent("## Nội dung thực hành...");
        lecture2.setClassroom(classroom);
        lecture2.setLectureDate(LocalDate.now().plusDays(3));
        if (scheduleToLink != null) lecture2.setSchedule(scheduleToLink);
        lectureRepository.save(lecture2);
    }

    private void seedLectureMaterials() {
        if (lectureMaterialRepository.count() > 0) {
            System.out.println("✅ [LectureSeeder] Lecture materials already exist. Skipping.");
            return;
        }

        System.out.println("📎 [LectureSeeder] Creating lecture materials...");
        
        List<Lecture> lectures = lectureRepository.findAll();
        String[] materialTypes = {"application/pdf", "application/vnd.ms-powerpoint", "video/mp4", "application/msword"};
        String[] materialNames = {"Bài giảng chương 1.pdf", "Slide thuyết trình.pptx", "Video bài giảng.mp4", "Tài liệu tham khảo.docx"};

        int materialsCreated = 0;
        for (Lecture lecture : lectures) {
            for (int i = 0; i < 2; i++) {
                LectureMaterial material = new LectureMaterial();
                material.setLecture(lecture);
                material.setFileName(materialNames[i % materialNames.length]);
                material.setContentType(materialTypes[i % materialTypes.length]);
                material.setFilePath("/materials/lecture_" + lecture.getId() + "_" + i + ".pdf");
                material.setFileSize(1024L * (500 + random.nextInt(2000)));
                lectureMaterialRepository.save(material);
                materialsCreated++;
            }
        }
        
        System.out.println("✅ [LectureSeeder] Created " + materialsCreated + " lecture materials for " + lectures.size() + " lectures");
    }

    private void seedLectureRecordings() {
        if (lectureRecordingRepository.count() > 0) {
            System.out.println("✅ [LectureSeeder] Lecture recordings already exist. Skipping.");
            return;
        }

        System.out.println("🎥 [LectureSeeder] Creating lecture recordings...");
        
        List<Lecture> lectures = lectureRepository.findAll();
        int recordingsCreated = 0;

        for (Lecture lecture : lectures) {
            if (random.nextDouble() < 0.7) { // 70% chance of having recording
                LectureRecording recording = new LectureRecording();
                recording.setLectureId(lecture.getId());
                recording.setTitle("Ghi âm " + lecture.getTitle());
                recording.setFilePath("/recordings/lecture_" + lecture.getId() + ".mp4");
                recording.setDurationMinutes(45 + random.nextInt(30));
                recording.setFileSize(1024L * 1024L * (200 + random.nextInt(800)));
                recording.setRecordingDate(lecture.getLectureDate().atTime(8, 30));
                lectureRecordingRepository.save(recording);
                recordingsCreated++;
            }
        }
        
        System.out.println("✅ [LectureSeeder] Created " + recordingsCreated + " lecture recordings for " + lectures.size() + " lectures");
    }
}
