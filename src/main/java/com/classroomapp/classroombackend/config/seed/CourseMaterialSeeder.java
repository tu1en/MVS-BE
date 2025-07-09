package com.classroomapp.classroombackend.config.seed;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.CourseMaterial;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.CourseMaterialRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseMaterialSeeder {

    private final CourseMaterialRepository courseMaterialRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    
    // Base directory for storing uploaded files - relative to project root
    private final String UPLOAD_DIR = "backend/doproject/uploads/materials";

    @Transactional
    public void seed(List<Classroom> classrooms) {
        System.out.println("Seeding course materials...");
        
        // Get teacher users for uploading materials
        List<User> teachers = userRepository.findByRoleId(2); // Teacher role ID
        
        if (teachers.isEmpty()) {
            System.out.println("No teachers found. Skipping course material seeding.");
            return;
        }

        // Seed materials for each classroom
        for (Classroom classroom : classrooms) {
            seedMaterialsForClassroom(classroom, teachers);
        }
        
        System.out.println("Course materials seeding completed.");
    }
    
    private void seedMaterialsForClassroom(Classroom classroom, List<User> teachers) {
        User teacher = getRandomTeacher(teachers);
        
        // Create directory for classroom materials if it doesn't exist
        String classroomDir = UPLOAD_DIR + "/" + classroom.getId();
        createDirectoryIfNotExists(classroomDir);
        
        // Create sample materials with actual files
        try {
            // PDF Document
            String pdfFileName = "textbook.pdf";
            String pdfPath = classroomDir + "/" + pdfFileName;
            createSampleFile(pdfPath, "Sample PDF content for " + classroom.getSubject());
            
            CourseMaterial pdfMaterial = new CourseMaterial();
            pdfMaterial.setTitle("Giáo trình " + classroom.getSubject());
            pdfMaterial.setDescription("Tài liệu học tập chính thức cho môn " + classroom.getSubject());
            pdfMaterial.setFilePath("/uploads/materials/" + classroom.getId() + "/" + pdfFileName); // Relative to server root
            pdfMaterial.setFileName(pdfFileName);
            pdfMaterial.setFileType("application/pdf");
            pdfMaterial.setFileSize(Files.size(Path.of(pdfPath)));
            pdfMaterial.setUploadDate(LocalDateTime.now().minusDays(random.nextInt(30)));
            pdfMaterial.setClassroomId(classroom.getId());
            pdfMaterial.setUploadedBy(teacher.getId());
            pdfMaterial.setIsPublic(true);
            pdfMaterial.setDownloadCount(random.nextInt(50));
            courseMaterialRepository.save(pdfMaterial);
            
            // Word Document
            String docxFileName = "exercises.docx";
            String docxPath = classroomDir + "/" + docxFileName;
            createSampleFile(docxPath, "Sample DOCX content for " + classroom.getSubject());
            
            CourseMaterial docxMaterial = new CourseMaterial();
            docxMaterial.setTitle("Bài tập " + classroom.getSubject());
            docxMaterial.setDescription("Bài tập thực hành cho môn " + classroom.getSubject());
            docxMaterial.setFilePath("/uploads/materials/" + classroom.getId() + "/" + docxFileName); // Relative to server root
            docxMaterial.setFileName(docxFileName);
            docxMaterial.setFileType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            docxMaterial.setFileSize(Files.size(Path.of(docxPath)));
            docxMaterial.setUploadDate(LocalDateTime.now().minusDays(random.nextInt(20)));
            docxMaterial.setClassroomId(classroom.getId());
            docxMaterial.setUploadedBy(teacher.getId());
            docxMaterial.setIsPublic(true);
            docxMaterial.setDownloadCount(random.nextInt(40));
            courseMaterialRepository.save(docxMaterial);
            
            // Text File (simpler than PowerPoint)
            String txtFileName = "lecture_notes.txt";
            String txtPath = classroomDir + "/" + txtFileName;
            createSampleFile(txtPath, "Sample lecture notes for " + classroom.getSubject() + "\n\n" +
                    "These are detailed notes about the course content.\n" +
                    "- Topic 1: Introduction\n" +
                    "- Topic 2: Main concepts\n" +
                    "- Topic 3: Advanced techniques\n\n" +
                    "Remember to review these notes before the exam!");
            
            CourseMaterial txtMaterial = new CourseMaterial();
            txtMaterial.setTitle("Ghi chú bài giảng " + classroom.getSubject());
            txtMaterial.setDescription("Ghi chú bài giảng cho môn " + classroom.getSubject());
            txtMaterial.setFilePath("/uploads/materials/" + classroom.getId() + "/" + txtFileName); // Relative to server root
            txtMaterial.setFileName(txtFileName);
            txtMaterial.setFileType("text/plain");
            txtMaterial.setFileSize(Files.size(Path.of(txtPath)));
            txtMaterial.setUploadDate(LocalDateTime.now().minusDays(random.nextInt(15)));
            txtMaterial.setClassroomId(classroom.getId());
            txtMaterial.setUploadedBy(teacher.getId());
            txtMaterial.setIsPublic(true);
            txtMaterial.setDownloadCount(random.nextInt(60));
            courseMaterialRepository.save(txtMaterial);
            
            // Try to copy a sample PDF from resources
            try {
                // Create a PDF file with fixed content for testing
                String realPdfFileName = "course_handbook.pdf";
                String realPdfPath = classroomDir + "/" + realPdfFileName;
                
                String pdfMinimalContent = 
                        "%PDF-1.4\n" +
                        "1 0 obj\n" +
                        "<</Type /Catalog /Pages 2 0 R>>\n" +
                        "endobj\n" +
                        "2 0 obj\n" +
                        "<</Type /Pages /Kids [3 0 R] /Count 1>>\n" +
                        "endobj\n" +
                        "3 0 obj\n" +
                        "<</Type /Page /Parent 2 0 R /Resources 4 0 R /MediaBox [0 0 612 792] /Contents 6 0 R>>\n" +
                        "endobj\n" +
                        "4 0 obj\n" +
                        "<</Font <</F1 5 0 R>>>>\n" +
                        "endobj\n" +
                        "5 0 obj\n" +
                        "<</Type /Font /Subtype /Type1 /BaseFont /Helvetica>>\n" +
                        "endobj\n" +
                        "6 0 obj\n" +
                        "<</Length 44>>\n" +
                        "stream\n" +
                        "BT /F1 24 Tf 100 700 Td (Course Handbook: " + classroom.getSubject() + ") Tj ET\n" +
                        "endstream\n" +
                        "endobj\n" +
                        "xref\n" +
                        "0 7\n" +
                        "0000000000 65535 f\n" +
                        "0000000009 00000 n\n" +
                        "0000000056 00000 n\n" +
                        "0000000111 00000 n\n" +
                        "0000000212 00000 n\n" +
                        "0000000250 00000 n\n" +
                        "0000000317 00000 n\n" +
                        "trailer\n" +
                        "<</Size 7 /Root 1 0 R>>\n" +
                        "startxref\n" +
                        "406\n" +
                        "%%EOF";
                
                createSampleFile(realPdfPath, pdfMinimalContent);
                
                CourseMaterial realPdfMaterial = new CourseMaterial();
                realPdfMaterial.setTitle("Sổ tay môn học " + classroom.getSubject());
                realPdfMaterial.setDescription("Sổ tay hướng dẫn chi tiết cho môn " + classroom.getSubject());
                realPdfMaterial.setFilePath("/uploads/materials/" + classroom.getId() + "/" + realPdfFileName); // Relative to server root
                realPdfMaterial.setFileName(realPdfFileName);
                realPdfMaterial.setFileType("application/pdf");
                realPdfMaterial.setFileSize(Files.size(Path.of(realPdfPath)));
                realPdfMaterial.setUploadDate(LocalDateTime.now().minusDays(random.nextInt(5)));
                realPdfMaterial.setClassroomId(classroom.getId());
                realPdfMaterial.setUploadedBy(teacher.getId());
                realPdfMaterial.setIsPublic(true);
                realPdfMaterial.setDownloadCount(random.nextInt(25));
                courseMaterialRepository.save(realPdfMaterial);
            } catch (Exception e) {
                log.error("Error creating PDF file: {}", e.getMessage(), e);
            }
            
        } catch (Exception e) {
            log.error("Error creating sample materials for classroom {}: {}", classroom.getId(), e.getMessage(), e);
        }
    }
    
    private User getRandomTeacher(List<User> teachers) {
        return teachers.get(random.nextInt(teachers.size()));
    }
    
    private void createDirectoryIfNotExists(String dirPath) {
        try {
            Files.createDirectories(Paths.get(dirPath));
        } catch (IOException e) {
            log.error("Error creating directory {}: {}", dirPath, e.getMessage(), e);
        }
    }
    
    private void createSampleFile(String filePath, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(content.getBytes());
        }
    }
} 