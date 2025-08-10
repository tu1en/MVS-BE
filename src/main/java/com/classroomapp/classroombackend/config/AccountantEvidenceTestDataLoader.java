package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation.ViolationSeverity;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation.ViolationStatus;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation.ViolationType;
import com.classroomapp.classroombackend.model.hrmanagement.ExplanationEvidence;
import com.classroomapp.classroombackend.model.hrmanagement.ViolationExplanation;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceViolationRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ExplanationEvidenceRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ViolationExplanationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

/**
 * Seeds minimal data for Accountant Evidence flows so the UI has content in
 * both tabs: "Minh chứng của tôi" and "Cần xem xét".
 */
@Component
@Order(1001) // run after main DataLoader and ViolationTestDataLoader
public class AccountantEvidenceTestDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AccountantEvidenceTestDataLoader.class);

    @Autowired private UserRepository userRepository;
    @Autowired private AttendanceViolationRepository violationRepository;
    @Autowired private ViolationExplanationRepository explanationRepository;
    @Autowired private ExplanationEvidenceRepository evidenceRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("[Seeder] AccountantEvidenceTestDataLoader starting...");

            // Prepare users
            User accountant = userRepository.findByEmail("bob.accountant@mvs.edu").orElse(null);
            if (accountant == null) {
                log.warn("Accountant test user not found; skipping accountant evidence seeding");
                return;
            }

            // Prefer an existing teacher; fallback to accountant for linkage
            User teacher = userRepository.findByEmail("john.teacher@mvs.edu")
                .orElse(userRepository.findByEmail("jane.teacher@mvs.edu").orElse(accountant));

            // Create or reuse a violation for the teacher within the last few days
            LocalDate vDate = LocalDate.now().minusDays(2);
            AttendanceViolation violation = violationRepository.findAll().stream()
                .filter(v -> v.getUser() != null && v.getUser().getId().equals(teacher.getId()))
                .findFirst()
                .orElseGet(() -> {
                    AttendanceViolation v = new AttendanceViolation();
                    v.setUser(teacher);
                    v.setViolationDate(vDate);
                    v.setViolationType(ViolationType.LATE_ARRIVAL);
                    v.setSeverity(ViolationSeverity.MINOR);
                    v.setExpectedTime(LocalTime.of(7, 30));
                    v.setActualTime(LocalTime.of(7, 50));
                    v.setDeviationMinutes(20);
                    v.setSystemDescription("Seed: Đi trễ 20 phút");
                    v.setStatus(ViolationStatus.PENDING_REVIEW);
                    v.setAutoDetected(true);
                    v.setDetectionTime(LocalDateTime.now().minusDays(2));
                    return violationRepository.save(v);
                });

            // Create or reuse an explanation for that violation, submitted by accountant
            ViolationExplanation explanation = violation.getExplanations() != null && !violation.getExplanations().isEmpty()
                ? violation.getExplanations().get(0)
                : null;
            if (explanation == null) {
                explanation = new ViolationExplanation();
                explanation.setViolation(violation);
                explanation.setSubmittedBy(accountant);
                explanation.setExplanationText("Minh chứng hỗ trợ từ kế toán cho vi phạm: " + violation.getDetailedDescription());
                explanation = explanationRepository.save(explanation);
            }

            // If we already seeded evidence, skip to avoid duplicates
            if (evidenceRepository.countByExplanationId(explanation.getId()) >= 2) {
                log.info("[Seeder] Evidence already present; skipping creation");
                return;
            }

            // 1) Pending review evidence (will appear in Cần xem xét)
            ExplanationEvidence pending = new ExplanationEvidence();
            pending.setExplanation(explanation);
            pending.setOriginalFilename("medical_note.pdf");
            pending.setFilePath("seed/evidence/medical_note.pdf");
            pending.setFileUrl("https://example.com/seed/medical_note.pdf");
            pending.setMimeType("application/pdf");
            pending.setFileType("pdf");
            pending.setFileSize(120_000L);
            pending.setEvidenceType(ExplanationEvidence.EvidenceType.MEDICAL_CERTIFICATE);
            pending.setDescription("Minh chứng hỗ trợ từ kế toán [ATTENDANCE] - Giấy khám bệnh cho phép vắng mặt");
            pending.setIsVerified(false);
            pending.setUploadedBy(accountant.getId());
            evidenceRepository.save(pending);

            // 2) Already reviewed evidence (appears dưới thống kê và tab của tôi)
            ExplanationEvidence reviewed = new ExplanationEvidence();
            reviewed.setExplanation(explanation);
            reviewed.setOriginalFilename("timesheet.png");
            reviewed.setFilePath("seed/evidence/timesheet.png");
            reviewed.setFileUrl("https://example.com/seed/timesheet.png");
            reviewed.setMimeType("image/png");
            reviewed.setFileType("png");
            reviewed.setFileSize(85_000L);
            reviewed.setEvidenceType(ExplanationEvidence.EvidenceType.IMAGE);
            reviewed.setDescription("Minh chứng hỗ trợ từ kế toán [ATTENDANCE] - Bảng chấm công bổ sung");
            reviewed.setUploadedBy(accountant.getId());
            reviewed.verify(accountant.getId());
            evidenceRepository.save(reviewed);

            log.info("[Seeder] Accountant evidence seeded successfully (violationId={}, explanationId={})",
                violation.getId(), explanation.getId());
        } catch (Exception e) {
            log.warn("[Seeder] AccountantEvidenceTestDataLoader error: {}", e.getMessage(), e);
        }
    }
}


