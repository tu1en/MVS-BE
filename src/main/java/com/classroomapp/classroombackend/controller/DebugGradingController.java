// package com.classroomapp.classroombackend.controller;

// import java.util.List;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;
// import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
// import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
// import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
// import com.classroomapp.classroombackend.service.SubmissionService;

// import lombok.extern.slf4j.Slf4j;

// /**
//  * Debug controller Ä‘á»ƒ kiá»ƒm tra grading data consistency
//  */
// @RestController
// @RequestMapping("/api/debug/grading")
// @CrossOrigin(origins = "*")
// @Slf4j
// public class DebugGradingController {

//     @Autowired
//     private SubmissionRepository submissionRepository;
    
//     @Autowired
//     private SubmissionService submissionService;

//     @Autowired
//     private com.classroomapp.classroombackend.service.GradeValidationService gradeValidationService;

//     /**
//      * Debug endpoint Ä‘á»ƒ kiá»ƒm tra submissions vá»›i score nhÆ°ng isGraded null
//      */
//     @GetMapping("/submissions/inconsistent")
//     public ResponseEntity<?> getInconsistentSubmissions() {
//         log.info("ðŸ” DEBUG: Checking for submissions with score but isGraded inconsistency");
        
//         // Get all submissions with scores
//         List<Submission> submissionsWithScores = submissionRepository.findAll().stream()
//                 .filter(s -> s.getScore() != null)
//                 .collect(Collectors.toList());
        
//         log.info("ðŸ” DEBUG: Found {} submissions with scores", submissionsWithScores.size());
        
//         // Convert to DTOs to see how isGraded is mapped
//         List<SubmissionDto> submissionDtos = submissionsWithScores.stream()
//                 .map(submission -> {
//                     try {
//                         return submissionService.GetSubmissionById(submission.getId());
//                     } catch (Exception e) {
//                         log.error("Error converting submission {}: {}", submission.getId(), e.getMessage());
//                         return null;
//                     }
//                 })
//                 .filter(dto -> dto != null)
//                 .collect(Collectors.toList());
        
//         log.info("ðŸ” DEBUG: Converted {} submissions to DTOs", submissionDtos.size());
        
//         // Log details for debugging
//         submissionDtos.forEach(dto -> {
//             log.info("ðŸ” DEBUG: Submission ID={}, Score={}, IsGraded={}, AssignmentId={}, StudentName={}", 
//                     dto.getId(), dto.getScore(), dto.getIsGraded(), dto.getAssignmentId(), dto.getStudentName());
//         });
        
//         return ResponseEntity.ok(submissionDtos);
//     }

//     /**
//      * Debug endpoint Ä‘á»ƒ kiá»ƒm tra submissions cho má»™t classroom cá»¥ thá»ƒ
//      */
//     @GetMapping("/classroom/{classroomId}/submissions")
//     public ResponseEntity<?> getClassroomSubmissions(@PathVariable Long classroomId) {
//         log.info("ðŸ” DEBUG: Getting submissions for classroom {}", classroomId);
        
//         List<Submission> submissions = submissionRepository.findAll().stream()
//                 .filter(s -> s.getAssignment().getClassroom().getId().equals(classroomId))
//                 .collect(Collectors.toList());
        
//         log.info("ðŸ” DEBUG: Found {} submissions for classroom {}", submissions.size(), classroomId);
        
//         // Create debug info
//         List<Object> debugInfo = submissions.stream()
//                 .map(submission -> {
//                     return new Object() {
//                         public final Long id = submission.getId();
//                         public final Long assignmentId = submission.getAssignment().getId();
//                         public final String assignmentTitle = submission.getAssignment().getTitle();
//                         public final String studentName = submission.getStudent().getFullName();
//                         public final Integer score = submission.getScore();
//                         public final String feedback = submission.getFeedback();
//                         public final boolean hasScore = submission.getScore() != null;
//                         public final boolean hasGradedAt = submission.getGradedAt() != null;
//                         public final boolean hasGradedBy = submission.getGradedBy() != null;
//                         public final String gradedByName = submission.getGradedBy() != null ? 
//                                 submission.getGradedBy().getFullName() : null;
//                     };
//                 })
//                 .collect(Collectors.toList());
        
//         return ResponseEntity.ok(debugInfo);
//     }

//     /**
//      * Debug endpoint Ä‘á»ƒ kiá»ƒm tra má»™t submission cá»¥ thá»ƒ
//      */
//     @GetMapping("/submission/{submissionId}")
//     public ResponseEntity<?> getSubmissionDetails(@PathVariable Long submissionId) {
//         log.info("ðŸ” DEBUG: Getting details for submission {}", submissionId);
        
//         try {
//             // Get raw entity
//             Submission submission = submissionRepository.findById(submissionId)
//                     .orElseThrow(() -> new RuntimeException("Submission not found"));
            
//             // Get DTO
//             SubmissionDto dto = submissionService.GetSubmissionById(submissionId);
            
//             // Create debug comparison
//             Object debugInfo = new Object() {
//                 public final String type = "SUBMISSION_DEBUG";
//                 public final Object rawEntity = new Object() {
//                     public final Long id = submission.getId();
//                     public final Integer score = submission.getScore();
//                     public final String feedback = submission.getFeedback();
//                     public final String gradedAt = submission.getGradedAt() != null ? 
//                             submission.getGradedAt().toString() : null;
//                     public final String gradedByName = submission.getGradedBy() != null ? 
//                             submission.getGradedBy().getFullName() : null;
//                     public final boolean hasScore = submission.getScore() != null;
//                 };
//                 public final Object mappedDto = new Object() {
//                     public final Long id = dto.getId();
//                     public final Integer score = dto.getScore();
//                     public final String feedback = dto.getFeedback();
//                     public final String gradedAt = dto.getGradedAt() != null ? 
//                             dto.getGradedAt().toString() : null;
//                     public final String gradedByName = dto.getGradedByName();
//                     public final Boolean isGraded = dto.getIsGraded();
//                 };
//             };
            
//             log.info("ðŸ” DEBUG: Submission {} - Raw hasScore: {}, DTO isGraded: {}", 
//                     submissionId, submission.getScore() != null, dto.getIsGraded());
            
//             return ResponseEntity.ok(debugInfo);
            
//         } catch (Exception e) {
//             log.error("ðŸ” DEBUG: Error getting submission {}: {}", submissionId, e.getMessage());
//             return ResponseEntity.badRequest().body("Error: " + e.getMessage());
//         }
//     }

//     /**
//      * Debug endpoint Ä‘á»ƒ fix existing data inconsistency
//      */
//     @GetMapping("/fix-data")
//     public ResponseEntity<?> fixDataInconsistency() {
//         log.info("ðŸ”§ DEBUG: Starting data consistency fix");
        
//         List<Submission> submissionsWithScores = submissionRepository.findAll().stream()
//                 .filter(s -> s.getScore() != null)
//                 .collect(Collectors.toList());
        
//         log.info("ðŸ”§ DEBUG: Found {} submissions with scores to verify", submissionsWithScores.size());
        
//         int fixedCount = 0;
//         for (Submission submission : submissionsWithScores) {
//             // Ensure gradedAt is set if score exists but gradedAt is null
//             if (submission.getScore() != null && submission.getGradedAt() == null) {
//                 submission.setGradedAt(java.time.LocalDateTime.now().minusDays(1)); // Set to yesterday
//                 submissionRepository.save(submission);
//                 fixedCount++;
//                 log.info("ðŸ”§ DEBUG: Fixed submission {} - set gradedAt", submission.getId());
//             }
//         }

//         log.info("ðŸ”§ DEBUG: Fixed {} submissions", fixedCount);

//         // Create final variables for use in anonymous class
//         final int totalCount = submissionsWithScores.size();
//         final int finalFixedCount = fixedCount;

//         return ResponseEntity.ok(new Object() {
//             public final String message = "Data consistency check completed";
//             public final int totalSubmissionsWithScores = totalCount;
//             public final int fixedSubmissions = finalFixedCount;
//         });
//     }

//     /**
//      * Debug endpoint Ä‘á»ƒ audit invalid scores (score > assignment.points)
//      */
//     @GetMapping("/audit-invalid-scores")
//     public ResponseEntity<?> auditInvalidScores() {
//         log.info("ðŸ” DEBUG: Starting audit for invalid scores");

//         int invalidCount = gradeValidationService.auditInvalidScores();

//         return ResponseEntity.ok(new Object() {
//             public final String message = "Invalid scores audit completed";
//             public final int invalidScoresFound = invalidCount;
//         });
//     }

//     /**
//      * Debug endpoint Ä‘á»ƒ fix invalid scores (cap them to assignment.points)
//      */
//     @GetMapping("/fix-invalid-scores")
//     public ResponseEntity<?> fixInvalidScores() {
//         log.info("ðŸ”§ DEBUG: Starting fix for invalid scores");

//         int fixedCount = gradeValidationService.fixInvalidScores();

//         return ResponseEntity.ok(new Object() {
//             public final String message = "Invalid scores fix completed";
//             public final int scoresFixed = fixedCount;
//         });
//     }

//     /**
//      * Debug endpoint Ä‘á»ƒ láº¥y submission vá»›i assignment details
//      */
//     @GetMapping("/submission-with-assignment/{submissionId}")
//     public ResponseEntity<?> getSubmissionWithAssignment(@PathVariable Long submissionId) {
//         log.info("ðŸ” DEBUG: Getting submission {} with assignment details", submissionId);

//         try {
//             // Get raw submission with assignment
//             Submission submission = submissionRepository.findById(submissionId)
//                     .orElseThrow(() -> new RuntimeException("Submission not found"));

//             Assignment assignment = submission.getAssignment();

//             return ResponseEntity.ok(new Object() {
//                 public final String type = "SUBMISSION_WITH_ASSIGNMENT_DEBUG";
//                 public final Object submissionData = new Object() {
//                     public final Long id = submission.getId();
//                     public final Integer score = submission.getScore();
//                     public final String feedback = submission.getFeedback();
//                     public final Boolean hasScore = submission.getScore() != null;
//                 };
//                 public final Object assignmentData = new Object() {
//                     public final Long id = assignment.getId();
//                     public final String title = assignment.getTitle();
//                     public final Integer points = assignment.getPoints();
//                     public final String classroomName = assignment.getClassroom().getName();
//                 };
//                 public final Object scoreAnalysis = new Object() {
//                     public final Integer rawScore = submission.getScore();
//                     public final Integer maxPoints = assignment.getPoints();
//                     public final Double normalizedScore = submission.getScore() != null && assignment.getPoints() != null && assignment.getPoints() > 0
//                             ? Math.round((submission.getScore().doubleValue() / assignment.getPoints()) * 10 * 100) / 100.0
//                             : null;
//                     public final String calculation = submission.getScore() != null && assignment.getPoints() != null
//                             ? String.format("(%d / %d) * 10 = %.2f", submission.getScore(), assignment.getPoints(),
//                                 submission.getScore().doubleValue() / assignment.getPoints() * 10)
//                             : "N/A";
//                 };
//             });

//         } catch (Exception e) {
//             log.error("ðŸ” DEBUG: Error getting submission with assignment {}: {}", submissionId, e.getMessage());
//             return ResponseEntity.badRequest().body("Error: " + e.getMessage());
//         }
//     }
// }
