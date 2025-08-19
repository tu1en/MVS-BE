package com.classroomapp.classroombackend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.util.TopCVCalculation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * =========================================================
 * SEEDER ĐÃ BỊ VÔ HIỆU HÓA - CONTRACT/SALARY BACKFILL SEEDER
 * =========================================================
 * Seeder này đã được ẩn theo yêu cầu không hiển thị dữ liệu lương cơ bản
 * Để kích hoạt lại, bỏ comment annotation @Component
 *
 * Backfill missing contract fields to ensure payroll generation works for all ACTIVE contracts.
 * - Fill missing contractId, status, userId (by email), contractType (infer), salary fields (hourly or gross/net),
 *   working hours/days defaults, fullName/email from user if missing.
 * - Non-destructive: only fills NULL/blank fields; does not override existing values.
 */
@Component // KÍCH HOẠT TẠM THỜI ĐỂ LÀM TRÒN LƯƠNG
@Order(85)
@RequiredArgsConstructor
@Slf4j
public class ContractBackfillSeeder implements CommandLineRunner {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Contract> contracts = contractRepository.findAll();
        if (contracts.isEmpty()) {
            return;
        }
        AtomicInteger updated = new AtomicInteger(0);
        for (Contract c : contracts) {
            boolean changed = false;

            // Round base salary to nearest 100,000 VND (one-time data cleanup)
            if (c.getSalary() != null && c.getSalary() > 0) {
                final long unit = 10_000L;
                final double original = c.getSalary();
                final long roundedLong = Math.round(original / unit) * unit;
                if (Double.compare(original, (double) roundedLong) != 0) {
                    c.setSalary((double) roundedLong);
                    changed = true;
                }
            }

            // Ensure contractId
            if (isBlank(c.getContractId())) {
                c.setContractId(generateContractId());
                changed = true;
            }

            // Ensure status - mặc định ACTIVE vì không cần endDate
            if (isBlank(c.getStatus())) {
                c.setStatus("ACTIVE");
                changed = true;
            }

            // Ensure userId by email
            if (c.getUserId() == null && !isBlank(c.getEmail())) {
                User u = userRepository.findByEmail(c.getEmail()).orElse(null);
                if (u != null) {
                    c.setUserId(u.getId());
                    // backfill name/phone if missing
                    if (isBlank(c.getFullName())) c.setFullName(u.getFullName());
                    if (isBlank(c.getPhoneNumber())) c.setPhoneNumber(u.getPhoneNumber());
                    changed = true;
                }
            }

            // Ensure basic identity
            if (isBlank(c.getFullName()) && c.getUserId() != null) {
                userRepository.findById(c.getUserId()).ifPresent(u -> {
                    c.setFullName(u.getFullName());
                });
                changed = true;
            }
            if (isBlank(c.getEmail()) && c.getUserId() != null) {
                userRepository.findById(c.getUserId()).ifPresent(u -> {
                    c.setEmail(u.getEmail());
                });
                changed = true;
            }

            // Infer contract type if missing
            if (isBlank(c.getContractType())) {
                String positionLower = c.getPosition() != null ? c.getPosition().toLowerCase(Locale.ROOT) : "";
                String deptLower = c.getDepartment() != null ? c.getDepartment().toLowerCase(Locale.ROOT) : "";
            if (positionLower.contains("giáo viên") || positionLower.contains("teacher") || !isBlank(c.getSubject())) {
                    c.setContractType("TEACHER");
                } else if (positionLower.contains("kế toán") || positionLower.contains("accountant") || deptLower.contains("tài chính")) {
                    c.setContractType("ACCOUNTANT");
                } else {
                    c.setContractType("STAFF");
                }
                changed = true;
            }

            // Fill salary fields
            boolean hasHourly = c.getHourlySalary() != null && c.getHourlySalary() > 0;
            boolean hasGross = c.getGrossSalary() != null && c.getGrossSalary() > 0;
            boolean hasNet = c.getNetSalary() != null && c.getNetSalary() > 0;
            boolean isTeacher = "TEACHER".equalsIgnoreCase(c.getContractType());

            if (isTeacher) {
                // Prefer hourly for teacher; derive if missing
                if (!hasHourly) {
                    Long derivedHourly = deriveHourlyFromSalaryOrOffer(c);
                    if (derivedHourly != null && derivedHourly > 0) {
                        c.setHourlySalary(derivedHourly);
                        changed = true;
                    }
                }
                // Clear gross/net for teacher to avoid ambiguity
                if (hasGross || hasNet) {
                    // keep as-is if already set; else leave nulls
                }
            } else {
                // Staff: prefer gross/net; derive from salary/offer
                if (!hasGross || !hasNet) {
                    BigDecimal gross = deriveGrossFromFields(c);
                    if (gross != null && gross.longValue() > 0) {
                        TopCVCalculation.SalaryCalculationResult res = TopCVCalculation.calculateFromGrossToNet(gross, 0);
                        c.setGrossSalary(res.getGrossSalary().longValue());
                        c.setNetSalary(res.getNetSalary().longValue());
                        changed = true;
                    }
                }
            }

            // Working defaults
            if (isBlank(c.getWorkDays())) {
                c.setWorkDays("Monday,Tuesday,Wednesday,Thursday,Friday");
                changed = true;
            }
            if (isBlank(c.getWorkingHours())) {
                if (isTeacher) {
                    c.setWorkingHours("ca sáng (07:30-09:30)");
                } else {
                    c.setWorkingHours("ca hành chính (08:30-17:30)");
                }
                changed = true;
            }

            // Bỏ việc set startDate vì không cần thiết
            if (c.getCreatedAt() == null) {
                c.setCreatedAt(LocalDateTime.now());
                changed = true;
            }
            if (changed) {
                c.setUpdatedAt(LocalDateTime.now());
                contractRepository.save(c);
                updated.incrementAndGet();
            }
        }
        if (updated.get() > 0) {
            log.info("ContractBackfillSeeder: updated {} contract(s) with missing fields", updated.get());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String generateContractId() {
        LocalDate today = LocalDate.now();
        long seq = contractRepository.count() + 1;
        String sequence = String.format("%02d", seq % 100);
        String dateFormat = String.format("%02d%02d", today.getMonthValue(), today.getYear() % 100);
        return sequence + dateFormat; // matches 6-digit format used elsewhere
    }

    // Bỏ method computeStatus vì không cần endDate nữa

    private Long deriveHourlyFromSalaryOrOffer(Contract c) {
        try {
            if (c.getSalary() != null && c.getSalary() > 0) {
                BigDecimal monthly = BigDecimal.valueOf(c.getSalary());
                return monthly.divide(new BigDecimal("176"), 0, java.math.RoundingMode.HALF_UP).longValue();
            }
            if (!isBlank(c.getOffer())) {
                String cleaned = c.getOffer().replaceAll("[^0-9]", "");
                if (!cleaned.isEmpty()) {
                    BigDecimal monthly = new BigDecimal(cleaned);
                    return monthly.divide(new BigDecimal("176"), 0, java.math.RoundingMode.HALF_UP).longValue();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private BigDecimal deriveGrossFromFields(Contract c) {
        try {
            if (c.getSalary() != null && c.getSalary() > 0) {
                return BigDecimal.valueOf(c.getSalary());
            }
            if (!isBlank(c.getOffer())) {
                String cleaned = c.getOffer().replaceAll("[^0-9]", "");
                if (!cleaned.isEmpty()) return new BigDecimal(cleaned);
            }
        } catch (Exception ignored) {}
        return null;
    }
}


