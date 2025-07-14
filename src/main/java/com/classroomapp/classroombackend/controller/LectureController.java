package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.LectureMaterialDto;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.LectureMaterial;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.LectureMaterialRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.classroomapp.classroombackend.service.LectureMaterialService;

@RestController
@RequestMapping("/api")
public class LectureController {

    @Autowired
    private LectureRepository lectureRepository;
    
    @Autowired
    private LectureMaterialRepository lectureMaterialRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private LectureMaterialService lectureMaterialService;

    // Get all lectures for a classroom - Changed endpoint to avoid conflict with CourseController
    @GetMapping("/lectures/classroom/{classroomId}")
    public ResponseEntity<List<LectureDto>> getLecturesByClassroom(@PathVariable Long classroomId) {
        List<Lecture> lectures = lectureRepository.findByClassroomId(classroomId);
        List<LectureDto> lectureDtos = new ArrayList<>();
        
        for (Lecture lecture : lectures) {
            LectureDto dto = new LectureDto();
            dto.setId(lecture.getId());
            dto.setTitle(lecture.getTitle());
            dto.setContent(lecture.getContent());
            dto.setLectureDate(lecture.getLectureDate());
            dto.setClassroomId(lecture.getClassroom().getId());
            
            // Get materials for this lecture
            List<LectureMaterial> materials = lectureMaterialRepository.findByLectureId(lecture.getId());
            List<LectureMaterialDto> materialDtos = new ArrayList<>();
            
            for (LectureMaterial material : materials) {
                LectureMaterialDto materialDto = new LectureMaterialDto();
                materialDto.setId(material.getId());
                materialDto.setFileName(material.getFileName());
                materialDto.setFilePath(material.getFilePath());
                materialDto.setFileSize(material.getFileSize());
                materialDto.setContentType(material.getContentType());
                materialDto.setDownloadUrl(material.getDownloadUrl());
                materialDto.setLectureId(lecture.getId());
                
                materialDtos.add(materialDto);
            }
            
            dto.setMaterials(materialDtos);
            lectureDtos.add(dto);
        }
        
        return ResponseEntity.ok(lectureDtos);
    }

    // Get a specific lecture
    @GetMapping("/lectures/{id}")
    public ResponseEntity<LectureDto> getLecture(@PathVariable Long id) {
        Optional<Lecture> lectureOpt = lectureRepository.findById(id);
        
        if (lectureOpt.isPresent()) {
            Lecture lecture = lectureOpt.get();
            LectureDto dto = new LectureDto();
            dto.setId(lecture.getId());
            dto.setTitle(lecture.getTitle());
            dto.setContent(lecture.getContent());
            dto.setLectureDate(lecture.getLectureDate());
            dto.setClassroomId(lecture.getClassroom().getId());
            
            // Get materials for this lecture
            List<LectureMaterial> materials = lectureMaterialRepository.findByLectureId(lecture.getId());
            List<LectureMaterialDto> materialDtos = new ArrayList<>();
            
            for (LectureMaterial material : materials) {
                LectureMaterialDto materialDto = new LectureMaterialDto();
                materialDto.setId(material.getId());
                materialDto.setFileName(material.getFileName());
                materialDto.setFilePath(material.getFilePath());
                materialDto.setFileSize(material.getFileSize());
                materialDto.setContentType(material.getContentType());
                materialDto.setDownloadUrl(material.getDownloadUrl());
                materialDto.setLectureId(lecture.getId());
                
                materialDtos.add(materialDto);
            }
            
            dto.setMaterials(materialDtos);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create a new lecture - New endpoint to match frontend
    @PostMapping("/lectures/classrooms/{classroomId}")
    public ResponseEntity<LectureDto> createLectureNew(@PathVariable Long classroomId, @RequestBody LectureDto lectureDto) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        System.out.println("🔍 [LectureController] Creating lecture for classroom " + classroomId + " by user: " + currentUserEmail);

        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomId);

        if (classroomOpt.isPresent()) {
            Classroom classroom = classroomOpt.get();

            // Authorization check: Only the teacher of the classroom can create lectures
            if (!classroom.getTeacher().getEmail().equals(currentUserEmail)) {
                System.out.println("❌ [LectureController] Access denied: User " + currentUserEmail + " is not the teacher of classroom " + classroomId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            System.out.println("✅ [LectureController] Authorization passed. Creating lecture...");

            Lecture lecture = new Lecture();
            lecture.setTitle(lectureDto.getTitle());
            lecture.setContent(lectureDto.getContent());
            lecture.setClassroom(classroom);
            // Set lectureDate - use provided date or default to today
            lecture.setLectureDate(lectureDto.getLectureDate() != null ? lectureDto.getLectureDate() : LocalDate.now());
            lecture.setCreatedAt(LocalDateTime.now());
            lecture.setUpdatedAt(LocalDateTime.now());

            Lecture savedLecture = lectureRepository.save(lecture);

            System.out.println("✅ [LectureController] Lecture created successfully with ID: " + savedLecture.getId());

            // Process materials if provided
            List<LectureMaterialDto> materialDtos = new ArrayList<>();
            if (lectureDto.getMaterials() != null && !lectureDto.getMaterials().isEmpty()) {
                System.out.println("🔍 [LectureController] Processing " + lectureDto.getMaterials().size() + " materials");
                for (LectureMaterialDto materialDto : lectureDto.getMaterials()) {
                    LectureMaterial material = new LectureMaterial();
                    material.setLecture(savedLecture);
                    material.setFileName(materialDto.getFileName());
                    material.setContentType(materialDto.getContentType());
                    material.setFileSize(materialDto.getFileSize());
                    material.setFilePath(materialDto.getFilePath());
                    material.setDownloadUrl(materialDto.getDownloadUrl());

                    LectureMaterial savedMaterial = lectureMaterialRepository.save(material);

                    LectureMaterialDto savedMaterialDto = new LectureMaterialDto();
                    savedMaterialDto.setId(savedMaterial.getId());
                    savedMaterialDto.setFileName(savedMaterial.getFileName());
                    savedMaterialDto.setContentType(savedMaterial.getContentType());
                    savedMaterialDto.setFileSize(savedMaterial.getFileSize());
                    savedMaterialDto.setDownloadUrl(savedMaterial.getDownloadUrl());
                    savedMaterialDto.setLectureId(savedLecture.getId());

                    materialDtos.add(savedMaterialDto);
                }
            }

            // Process YouTube URL if provided
            if (lectureDto.getYoutubeEmbedUrl() != null && !lectureDto.getYoutubeEmbedUrl().trim().isEmpty()) {
                System.out.println("🔍 [LectureController] Processing YouTube video: " + lectureDto.getYoutubeEmbedUrl());
                LectureMaterial youtubeMaterial = new LectureMaterial();
                youtubeMaterial.setLecture(savedLecture);
                youtubeMaterial.setFileName("YouTube Video");
                youtubeMaterial.setContentType("video/youtube");
                youtubeMaterial.setDownloadUrl(lectureDto.getYoutubeEmbedUrl());
                youtubeMaterial.setFileSize(0L);

                LectureMaterial savedYoutubeMaterial = lectureMaterialRepository.save(youtubeMaterial);

                LectureMaterialDto youtubeMaterialDto = new LectureMaterialDto();
                youtubeMaterialDto.setId(savedYoutubeMaterial.getId());
                youtubeMaterialDto.setFileName(savedYoutubeMaterial.getFileName());
                youtubeMaterialDto.setContentType(savedYoutubeMaterial.getContentType());
                youtubeMaterialDto.setDownloadUrl(savedYoutubeMaterial.getDownloadUrl());
                youtubeMaterialDto.setLectureId(savedLecture.getId());

                materialDtos.add(youtubeMaterialDto);
            }

            LectureDto responseDto = new LectureDto();
            responseDto.setId(savedLecture.getId());
            responseDto.setTitle(savedLecture.getTitle());
            responseDto.setContent(savedLecture.getContent());
            responseDto.setLectureDate(savedLecture.getLectureDate());
            responseDto.setClassroomId(classroomId);
            responseDto.setMaterials(materialDtos);

            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } else {
            System.out.println("❌ [LectureController] Classroom not found with ID: " + classroomId);
            return ResponseEntity.notFound().build();
        }
    }

    // Create a new lecture - Original endpoint
    @PostMapping("/courses/{classroomId}/lectures")
    public ResponseEntity<LectureDto> createLecture(@PathVariable Long classroomId, @RequestBody LectureDto lectureDto) {
        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomId);
        
        if (classroomOpt.isPresent()) {
            Classroom classroom = classroomOpt.get();
            
            Lecture lecture = new Lecture();
            lecture.setTitle(lectureDto.getTitle());
            lecture.setContent(lectureDto.getContent());
            lecture.setLectureDate(lectureDto.getLectureDate() != null ? lectureDto.getLectureDate() : LocalDate.now());
            lecture.setClassroom(classroom);
            lecture.setCreatedAt(LocalDateTime.now());
            
            Lecture savedLecture = lectureRepository.save(lecture);
            
            LectureDto responseDto = new LectureDto();
            responseDto.setId(savedLecture.getId());
            responseDto.setTitle(savedLecture.getTitle());
            responseDto.setContent(savedLecture.getContent());
            responseDto.setLectureDate(savedLecture.getLectureDate());
            responseDto.setClassroomId(classroom.getId());
            
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update a lecture
    @PutMapping("/courses/{classroomId}/lectures/{id}")
    public ResponseEntity<LectureDto> updateLecture(
            @PathVariable Long classroomId, 
            @PathVariable Long id, 
            @RequestBody LectureDto lectureDto) {
        
        Optional<Lecture> lectureOpt = lectureRepository.findById(id);
        
        if (lectureOpt.isPresent()) {
            Lecture lecture = lectureOpt.get();
            
            // Verify this lecture belongs to the specified classroom
            if (!lecture.getClassroom().getId().equals(classroomId)) {
                return ResponseEntity.badRequest().build();
            }
            
            // Update fields
            lecture.setTitle(lectureDto.getTitle());
            lecture.setContent(lectureDto.getContent());
            if (lectureDto.getLectureDate() != null) {
                lecture.setLectureDate(lectureDto.getLectureDate());
            }
            lecture.setUpdatedAt(LocalDateTime.now());
            
            Lecture updatedLecture = lectureRepository.save(lecture);
            
            LectureDto responseDto = new LectureDto();
            responseDto.setId(updatedLecture.getId());
            responseDto.setTitle(updatedLecture.getTitle());
            responseDto.setContent(updatedLecture.getContent());
            responseDto.setLectureDate(updatedLecture.getLectureDate());
            responseDto.setClassroomId(classroomId);
            
            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a lecture
    @DeleteMapping("/courses/{classroomId}/lectures/{id}")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long classroomId, @PathVariable Long id) {
        Optional<Lecture> lectureOpt = lectureRepository.findById(id);
        
        if (lectureOpt.isPresent()) {
            Lecture lecture = lectureOpt.get();
            
            // Verify this lecture belongs to the specified classroom
            if (!lecture.getClassroom().getId().equals(classroomId)) {
                return ResponseEntity.badRequest().build();
            }
            
            // Delete associated materials first
            List<LectureMaterial> materials = lectureMaterialRepository.findByLectureId(id);
            for (LectureMaterial material : materials) {
                lectureMaterialRepository.delete(material);
                
                // Optionally delete the actual file
                // fileStorageService.deleteFile(material.getFilePath());
            }
            
            // Now delete the lecture
            lectureRepository.delete(lecture);
            
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get materials for a lecture
    @GetMapping("/lectures/{lectureId}/materials")
    public ResponseEntity<List<LectureMaterialDto>> getLectureMaterials(@PathVariable Long lectureId) {
        List<LectureMaterial> materials = lectureMaterialRepository.findByLectureId(lectureId);
        List<LectureMaterialDto> materialDtos = new ArrayList<>();

        for (LectureMaterial material : materials) {
            LectureMaterialDto dto = new LectureMaterialDto();
            dto.setId(material.getId());
            dto.setFileName(material.getFileName());
            dto.setFilePath(material.getFilePath());
            dto.setFileSize(material.getFileSize());
            dto.setContentType(material.getContentType());
            dto.setDownloadUrl(material.getDownloadUrl());
            dto.setLectureId(lectureId);

            materialDtos.add(dto);
        }

        return ResponseEntity.ok(materialDtos);
    }

    // Download lecture material
    @GetMapping("/lecture-materials/download/{materialId}")
    public ResponseEntity<Resource> downloadLectureMaterial(@PathVariable Long materialId) {
        return lectureMaterialService.getFile(materialId);
    }

    // Upload material for a lecture
    @PostMapping("/lectures/{lectureId}/materials")
    public ResponseEntity<LectureMaterialDto> uploadMaterial(
            @PathVariable Long lectureId,
            @RequestParam("file") MultipartFile file) {
        
        Optional<Lecture> lectureOpt = lectureRepository.findById(lectureId);
        
        if (lectureOpt.isPresent()) {
            try {
                // Save the file
                FileUploadResponse fileResponse = fileStorageService.save(file, "lectures");
                String fileName = fileResponse.getFileName();
                String filePath = fileName; // Simplified for example
                String downloadUrl = fileResponse.getFileUrl();
                
                // Create material record
                LectureMaterial material = new LectureMaterial();
                material.setLecture(lectureOpt.get());
                material.setFileName(file.getOriginalFilename());
                material.setFilePath(filePath);
                material.setFileSize(file.getSize());
                material.setContentType(file.getContentType());
                material.setDownloadUrl(downloadUrl);
                material.setCreatedAt(LocalDateTime.now());
                
                LectureMaterial savedMaterial = lectureMaterialRepository.save(material);
                
                // Create response DTO
                LectureMaterialDto dto = new LectureMaterialDto();
                dto.setId(savedMaterial.getId());
                dto.setFileName(savedMaterial.getFileName());
                dto.setFilePath(savedMaterial.getFilePath());
                dto.setFileSize(savedMaterial.getFileSize());
                dto.setContentType(savedMaterial.getContentType());
                dto.setDownloadUrl(savedMaterial.getDownloadUrl());
                dto.setLectureId(lectureId);
                return new ResponseEntity<>(dto, HttpStatus.CREATED);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a material
    @DeleteMapping("/lectures/materials/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId) {
        Optional<LectureMaterial> materialOpt = lectureMaterialRepository.findById(materialId);
        
        if (materialOpt.isPresent()) {
            LectureMaterial material = materialOpt.get();
            
            // Delete the material record
            lectureMaterialRepository.delete(material);
            
            // Optionally delete the actual file
            // fileStorageService.deleteFile(material.getFilePath());
            
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Create sample lectures for a classroom
    @PostMapping("/courses/{classroomId}/sample-lectures")
    public ResponseEntity<List<LectureDto>> createSampleLectures(@PathVariable Long classroomId) {
        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomId);
        
        if (classroomOpt.isPresent()) {
            Classroom classroom = classroomOpt.get();
            List<LectureDto> createdLectures = new ArrayList<>();
            
            try {
                // Bài giảng 1: Giới thiệu khóa học
                Lecture lecture1 = new Lecture();
                lecture1.setTitle("Giới thiệu khóa học");
                lecture1.setContent("# Giới thiệu khóa học\n\n## Mục tiêu\n\n- Hiểu được các khái niệm cơ bản\n- Nắm vững các kỹ thuật nền tảng\n- Áp dụng kiến thức vào thực tiễn\n\n## Nội dung chính\n\n1. Tổng quan về môn học\n2. Các khái niệm cơ bản\n3. Phương pháp học tập hiệu quả");
                lecture1.setLectureDate(LocalDate.now());
                lecture1.setClassroom(classroom);
                lecture1.setCreatedAt(LocalDateTime.now());
                
                Lecture savedLecture1 = lectureRepository.save(lecture1);
                
                LectureDto dto1 = new LectureDto();
                dto1.setId(savedLecture1.getId());
                dto1.setTitle(savedLecture1.getTitle());
                dto1.setContent(savedLecture1.getContent());
                dto1.setLectureDate(savedLecture1.getLectureDate());
                dto1.setClassroomId(classroom.getId());
                
                createdLectures.add(dto1);
                
                // Bài giảng 2: Lý thuyết cơ bản
                Lecture lecture2 = new Lecture();
                lecture2.setTitle("Lý thuyết cơ bản");
                lecture2.setContent("# Lý thuyết cơ bản\n\n## Các khái niệm chính\n\n- Định nghĩa và ý nghĩa\n- Cấu trúc và thành phần\n- Quy trình hoạt động\n\n## Ứng dụng thực tiễn\n\n1. Phân tích trường hợp điển hình\n2. Giải quyết vấn đề thực tế\n3. Tối ưu hóa quy trình");
                lecture2.setLectureDate(LocalDate.now().plusDays(7));
                lecture2.setClassroom(classroom);
                lecture2.setCreatedAt(LocalDateTime.now());
                
                Lecture savedLecture2 = lectureRepository.save(lecture2);
                
                LectureDto dto2 = new LectureDto();
                dto2.setId(savedLecture2.getId());
                dto2.setTitle(savedLecture2.getTitle());
                dto2.setContent(savedLecture2.getContent());
                dto2.setLectureDate(savedLecture2.getLectureDate());
                dto2.setClassroomId(classroom.getId());
                
                createdLectures.add(dto2);
                
                // Bài giảng 3: Thực hành và ứng dụng
                Lecture lecture3 = new Lecture();
                lecture3.setTitle("Thực hành và ứng dụng");
                lecture3.setContent("# Thực hành và ứng dụng\n\n## Mục tiêu\n\n- Áp dụng lý thuyết vào thực tiễn\n- Phát triển kỹ năng thực hành\n- Giải quyết các bài toán thực tế\n\n## Nội dung\n\n1. Bài tập thực hành\n2. Dự án nhóm\n3. Đánh giá và phản hồi");
                lecture3.setLectureDate(LocalDate.now().plusDays(14));
                lecture3.setClassroom(classroom);
                lecture3.setCreatedAt(LocalDateTime.now());
                
                Lecture savedLecture3 = lectureRepository.save(lecture3);
                
                LectureDto dto3 = new LectureDto();
                dto3.setId(savedLecture3.getId());
                dto3.setTitle(savedLecture3.getTitle());
                dto3.setContent(savedLecture3.getContent());
                dto3.setLectureDate(savedLecture3.getLectureDate());
                dto3.setClassroomId(classroom.getId());
                
                createdLectures.add(dto3);
                
                return new ResponseEntity<>(createdLectures, HttpStatus.CREATED);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
