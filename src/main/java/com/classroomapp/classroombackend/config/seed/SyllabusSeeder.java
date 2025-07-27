package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Syllabus;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.SyllabusRepository;

@Component
public class SyllabusSeeder {

    @Autowired
    private SyllabusRepository syllabusRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Transactional
    public void seed() {
        if (syllabusRepository.count() == 0) {
            System.out.println("Seeding syllabi...");
            List<Classroom> classrooms = classroomRepository.findAll();
            if (!classrooms.isEmpty()) {
                Classroom firstClassroom = classrooms.get(0);
                Syllabus syllabus1 = new Syllabus();
                syllabus1.setTitle("Chương 1: Giới thiệu");
                syllabus1.setDescription("Tổng quan về môn học và các khái niệm cơ bản.");
                syllabus1.setClassroom(firstClassroom);
                syllabusRepository.save(syllabus1);

                Syllabus syllabus2 = new Syllabus();
                syllabus2.setTitle("Chương 2: Đi sâu vào chi tiết");
                syllabus2.setDescription("Phân tích các chủ đề nâng cao.");
                syllabus2.setClassroom(firstClassroom);
                syllabusRepository.save(syllabus2);
            }
            System.out.println("Syllabi seeded.");
        }
    }
}