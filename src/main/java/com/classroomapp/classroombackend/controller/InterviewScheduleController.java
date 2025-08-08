package com.classroomapp.classroombackend.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.InterviewScheduleDto;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.InterviewScheduleService;
import com.classroomapp.classroombackend.service.UserServiceExtension;
import com.classroomapp.classroombackend.util.TopCVCalculation;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/interview-schedules")
@RequiredArgsConstructor
public class InterviewScheduleController {
    private final InterviewScheduleService interviewService;
    private final EmailService emailService;
    private final UserServiceExtension userService;

    // Custom error response class
    public static class ErrorResponse {
        private String error;
        private String message;
        
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestParam Long applicationId,
                                                       @RequestParam String startTime,
                                                       @RequestParam String endTime) {
        System.out.println("=== Creating Interview Schedule ===");
        System.out.println("Application ID: " + applicationId);
        System.out.println("Start Time: " + startTime);
        System.out.println("End Time: " + endTime);
        
        // startTime, endTime dạng string không có timezone
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        
        System.out.println("Parsed Start: " + start);
        System.out.println("Parsed End: " + end);
        System.out.println("CREATE - Duration: " + java.time.Duration.between(start, end).toMinutes() + " minutes");
        
        // Kiểm tra không cho phép xếp lịch trong quá khứ
        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now)) {
            System.out.println("ERROR: Start time is in the past!");
            return ResponseEntity.badRequest().body(new ErrorResponse("PAST_TIME", "Thời gian bắt đầu không được trong quá khứ!"));
        }
        
        // Kiểm tra cùng ngày
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            System.out.println("ERROR: Start and end time must be on the same day!");
            return ResponseEntity.badRequest().body(new ErrorResponse("DIFFERENT_DAYS", "Thời gian bắt đầu và kết thúc phải trong cùng một ngày!"));
        }
        
        // Kiểm tra không quá 4 tiếng
        long durationMinutes = java.time.Duration.between(start, end).toMinutes();
        if (durationMinutes > 240) { // 4 hours = 240 minutes
            System.out.println("ERROR: Interview duration cannot exceed 4 hours!");
            return ResponseEntity.badRequest().body(new ErrorResponse("DURATION_TOO_LONG", "Thời gian phỏng vấn không được quá 4 tiếng!"));
        }
        
        // Kiểm tra thời gian bắt đầu phải trước thời gian kết thúc
        if (!start.isBefore(end)) {
            System.out.println("ERROR: Start time must be before end time!");
            return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_TIME_ORDER", "Thời gian bắt đầu phải trước thời gian kết thúc!"));
        }
        
        // Kiểm tra trùng lịch
        System.out.println("Checking for conflicts...");
        if (interviewService.hasConflict(start, end, applicationId)) {
            System.out.println("ERROR: Conflict detected!");
            return ResponseEntity.badRequest().body(new ErrorResponse("SCHEDULE_CONFLICT", "Thời gian phỏng vấn bị trùng với lịch phỏng vấn khác!"));
        }
        
        System.out.println("No conflicts, creating schedule...");
        InterviewScheduleDto dto = interviewService.create(applicationId, start, end);
        
        // Gửi mail thông báo lịch phỏng vấn
        String interviewTime = start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " - " + end.format(DateTimeFormatter.ofPattern("HH:mm"));
        emailService.sendInterviewScheduledEmail(dto.getApplicantEmail(), dto.getApplicantName(), dto.getJobTitle(), interviewTime);
        
        System.out.println("Schedule created successfully: " + dto.getId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/check-conflict")
    public ResponseEntity<Boolean> checkConflict(@RequestParam String startTime,
                                               @RequestParam String endTime,
                                               @RequestParam(required = false) Long excludeApplicationId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        
        boolean hasConflict = interviewService.hasConflict(start, end, excludeApplicationId);
        return ResponseEntity.ok(hasConflict);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                                     @RequestParam String startTime,
                                                     @RequestParam String endTime) {
        System.out.println("=== Updating Interview Schedule ===");
        System.out.println("Interview ID: " + id);
        System.out.println("Start Time: " + startTime);
        System.out.println("End Time: " + endTime);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        System.out.println("Parsed Start: " + start);
        System.out.println("Parsed End: " + end);

        // Kiểm tra không cho phép xếp lịch trong quá khứ
        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now)) {
            System.out.println("ERROR: Start time is in the past!");
            return ResponseEntity.badRequest().body(new ErrorResponse("PAST_TIME", "Thời gian bắt đầu không được trong quá khứ!"));
        }
        
        // Kiểm tra cùng ngày
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            System.out.println("ERROR: Start and end time must be on the same day!");
            return ResponseEntity.badRequest().body(new ErrorResponse("DIFFERENT_DAYS", "Thời gian bắt đầu và kết thúc phải trong cùng một ngày!"));
        }
        
        // Kiểm tra không quá 4 tiếng
        long durationMinutes = java.time.Duration.between(start, end).toMinutes();
        System.out.println("UPDATE - Duration check: " + durationMinutes + " minutes (max 240)");
        if (durationMinutes > 240) { // 4 hours = 240 minutes
            System.out.println("ERROR: Interview duration cannot exceed 4 hours!");
            return ResponseEntity.badRequest().body(new ErrorResponse("DURATION_TOO_LONG", "Thời gian phỏng vấn không được quá 4 tiếng!"));
        }
        
        // Kiểm tra thời gian bắt đầu phải trước thời gian kết thúc
        System.out.println("UPDATE - Time order check: start=" + start + ", end=" + end + ", start.isBefore(end)=" + start.isBefore(end));
        if (!start.isBefore(end)) {
            System.out.println("ERROR: Start time must be before end time!");
            return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_TIME_ORDER", "Thời gian bắt đầu phải trước thời gian kết thúc!"));
        }

        // Lấy thông tin interview hiện tại
        InterviewScheduleDto currentInterview = interviewService.getById(id);
        if (currentInterview == null) {
            System.out.println("ERROR: Interview not found!");
            return ResponseEntity.notFound().build();
        }

        System.out.println("Current interview application ID: " + currentInterview.getApplicationId());

        // Kiểm tra trùng lịch (loại trừ lịch hiện tại)
        System.out.println("Checking for conflicts...");
        if (interviewService.hasConflict(start, end, currentInterview.getApplicationId())) {
            System.out.println("ERROR: Conflict detected!");
            return ResponseEntity.badRequest().body(new ErrorResponse("SCHEDULE_CONFLICT", "Thời gian phỏng vấn bị trùng với lịch phỏng vấn khác!"));
        }

        System.out.println("No conflicts, updating schedule...");
        InterviewScheduleDto updated = interviewService.update(id, start, end);
        System.out.println("Schedule updated successfully: " + updated.getId());
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<InterviewScheduleDto>> getAll() {
        return ResponseEntity.ok(interviewService.getAll());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InterviewScheduleDto>> getPending() {
        List<InterviewScheduleDto> all = interviewService.getAll();
        LocalDateTime now = LocalDateTime.now();
        
        // Tự động chuyển trạng thái các lịch phỏng vấn đã quá giờ
        all.forEach(interview -> {
            if (interview.getEndTime().isBefore(now) && 
                ("SCHEDULED".equals(interview.getStatus()) || interview.getStatus() == null)) {
                interviewService.updateStatus(interview.getId(), "PENDING", null);
            }
        });
        
        // Lọc các lịch phỏng vấn chờ (SCHEDULED hoặc PENDING)
        List<InterviewScheduleDto> pending = all.stream()
            .filter(i -> "PENDING".equals(i.getStatus()) || "SCHEDULED".equals(i.getStatus()))
            .toList();
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/by-job/{jobPositionId}")
    public ResponseEntity<List<InterviewScheduleDto>> getByJob(@PathVariable Long jobPositionId) {
        return ResponseEntity.ok(interviewService.getByJobPosition(jobPositionId));
    }

    @GetMapping("/by-application/{applicationId}")
    public ResponseEntity<List<InterviewScheduleDto>> getByApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(interviewService.getByApplication(applicationId));
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<InterviewScheduleDto>> getAcceptedInterviews() {
        List<InterviewScheduleDto> acceptedInterviews = interviewService.getAcceptedInterviews();
        return ResponseEntity.ok(acceptedInterviews);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String result) {
        interviewService.updateStatus(id, status, result);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/offer")
    public ResponseEntity<?> updateOffer(@PathVariable Long id, @RequestBody OfferUpdateRequest request) {
        interviewService.updateOffer(id, request.getOffer());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/evaluation")
    public ResponseEntity<?> updateEvaluation(@PathVariable Long id, @RequestBody EvaluationUpdateRequest request) {
        interviewService.updateEvaluation(id, request.getEvaluation());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/resend-offer")
    public ResponseEntity<?> resendOffer(@PathVariable Long id, @RequestBody OfferUpdateRequest request) {
        // Lấy thông tin interview để gửi email (không cập nhật offer trong database)
        InterviewScheduleDto interview = interviewService.getById(id);
        if (interview != null) {
            emailService.sendOfferResendEmailWithDetails(
                interview.getApplicantEmail(), 
                interview.getApplicantName(), 
                interview.getJobTitle(), 
                request.getOffer(),
                request.getSalaryDetails()
            );
        }
        
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/check-account")
    public ResponseEntity<?> checkAccount(@PathVariable Long id) {
        InterviewScheduleDto interview = interviewService.getById(id);
        if (interview == null) {
            return ResponseEntity.notFound().build();
        }
        
        boolean hasAccount = userService.userExists(interview.getApplicantEmail());
        boolean hasContract = false; // Mặc định chưa có hợp đồng
        
        if (hasAccount) {
            // Kiểm tra trạng thái hợp đồng từ user status
            hasContract = userService.hasActiveContract(interview.getApplicantEmail());
        }
        
        return ResponseEntity.ok(new AccountCheckResponse(hasAccount, hasContract));
    }

    @PutMapping("/{id}/result")
    public ResponseEntity<?> setResult(@PathVariable Long id, @RequestBody InterviewResultDto body) {
        interviewService.updateStatus(id, body.getStatus(), body.getResult());
        InterviewScheduleDto interview = interviewService.getAll().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
        if (interview == null) return ResponseEntity.notFound().build();
        
        // Gửi mail kết quả
        if ("APPROVED".equals(body.getStatus()) || "ACCEPTED".equals(body.getStatus())) {
            emailService.sendEmail(interview.getApplicantEmail(), "Kết quả phỏng vấn", "Chúc mừng bạn đã vượt qua phỏng vấn cho vị trí: " + interview.getJobTitle());
            
            // Tạo user mới với trạng thái chưa có hợp đồng nếu cần
            if (body.getCreateAccount() != null && body.getCreateAccount()) {
                userService.createUserWithoutContract(interview.getApplicantEmail(), interview.getApplicantName(), "TEACHER");
            } else {
                // Tạo user mới với trạng thái chưa có hợp đồng (logic mặc định)
                userService.createUserWithoutContract(interview.getApplicantEmail(), interview.getApplicantName(), "TEACHER");
            }
        } else if ("REJECTED".equals(body.getStatus())) {
            // Gửi mail từ chối với evaluation
            InterviewScheduleDto interviewDto = interviewService.getAll().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
            if (interviewDto != null) {
                emailService.sendInterviewRejectionEmail(interview.getApplicantEmail(), interview.getApplicantName(), interview.getJobTitle(), body.getResult(), interviewDto.getEvaluation());
            } else {
                emailService.sendInterviewRejectionEmail(interview.getApplicantEmail(), interview.getApplicantName(), interview.getJobTitle(), body.getResult());
            }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/salary-calculation")
    public ResponseEntity<TopCVCalculation.SalaryCalculationResult> calculateSalaryDetails(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int numberOfDependents) {
        try {
            InterviewScheduleDto interview = interviewService.getById(id);
            if (interview == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Parse offer amount from string to BigDecimal
            BigDecimal grossSalary = null;
            if (interview.getOffer() != null && !interview.getOffer().trim().isEmpty()) {
                try {
                    // Remove any non-numeric characters and parse
                    String cleanOffer = interview.getOffer().replaceAll("[^0-9]", "");
                    grossSalary = new BigDecimal(cleanOffer);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                return ResponseEntity.badRequest().body(null);
            }
            
            // Calculate salary details using TopCVCalculation
            TopCVCalculation.SalaryCalculationResult result = 
                TopCVCalculation.calculateFromGrossToNet(grossSalary, numberOfDependents);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/calculate-gross-from-net")
    public ResponseEntity<TopCVCalculation.SalaryCalculationResult> calculateGrossFromNet(
            @PathVariable Long id,
            @RequestBody NetToGrossRequest request) {
        try {
            InterviewScheduleDto interview = interviewService.getById(id);
            if (interview == null) {
                return ResponseEntity.notFound().build();
            }
            
            BigDecimal netSalary = new BigDecimal(request.getNetSalary());
            TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromNetToGross(netSalary, request.getNumberOfDependents());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

class InterviewResultDto {
    private String status;
    private String result;
    private Boolean createAccount;
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public Boolean getCreateAccount() { return createAccount; }
    public void setCreateAccount(Boolean createAccount) { this.createAccount = createAccount; }
}

class OfferUpdateRequest {
    private String offer;
    private Object salaryDetails;
    
    public String getOffer() { return offer; }
    public void setOffer(String offer) { this.offer = offer; }
    
    public Object getSalaryDetails() { return salaryDetails; }
    public void setSalaryDetails(Object salaryDetails) { this.salaryDetails = salaryDetails; }
}

class EvaluationUpdateRequest {
    private String evaluation;
    public String getEvaluation() { return evaluation; }
    public void setEvaluation(String evaluation) { this.evaluation = evaluation; }
}

class NetToGrossRequest {
    private String netSalary;
    private int numberOfDependents = 0;
    
    public String getNetSalary() { return netSalary; }
    public void setNetSalary(String netSalary) { this.netSalary = netSalary; }
    
    public int getNumberOfDependents() { return numberOfDependents; }
    public void setNumberOfDependents(int numberOfDependents) { this.numberOfDependents = numberOfDependents; }
} 

class AccountCheckResponse {
    private boolean hasAccount;
    private boolean hasContract;
    
    public AccountCheckResponse(boolean hasAccount, boolean hasContract) {
        this.hasAccount = hasAccount;
        this.hasContract = hasContract;
    }
    
    public boolean isHasAccount() { return hasAccount; }
    public void setHasAccount(boolean hasAccount) { this.hasAccount = hasAccount; }
    public boolean isHasContract() { return hasContract; }
    public void setHasContract(boolean hasContract) { this.hasContract = hasContract; }
} 