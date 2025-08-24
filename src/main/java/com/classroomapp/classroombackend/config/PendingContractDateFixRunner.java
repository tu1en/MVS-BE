package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.repository.ContractRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * One-time, idempotent data fix to enforce start_date = 01/09/<current_year>
 * and end_date = start_date + 90 days for all PENDING contracts.
 *
 * Safe to keep enabled: it only updates when values differ from the target.
 */
@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
public class PendingContractDateFixRunner implements CommandLineRunner {

    private final ContractRepository contractRepository;

    @Override
    @Transactional
    public void run(String... args) {
        final String targetStatus = "PENDING";
        final int currentYear = LocalDate.now().getYear();
        final LocalDate septFirst = LocalDate.of(currentYear, 9, 1);

        List<Contract> pendings = contractRepository.findByStatusOrderByCreatedAtDesc(targetStatus);
        if (pendings == null || pendings.isEmpty()) {
            return;
        }

        List<Contract> toUpdate = new ArrayList<>();
        for (Contract c : pendings) {
            LocalDate start = c.getStartDate();
            LocalDate desiredStart = septFirst;
            LocalDate desiredEnd = desiredStart.plusDays(90);

            boolean needsStartFix = (start == null) || !desiredStart.equals(start);
            boolean needsEndFix = (c.getEndDate() == null) || !desiredEnd.equals(c.getEndDate());

            if (needsStartFix || needsEndFix) {
                c.setStartDate(desiredStart);
                c.setEndDate(desiredEnd);
                toUpdate.add(c);
            }
        }

        if (!toUpdate.isEmpty()) {
            contractRepository.saveAll(toUpdate);
            log.info("PendingContractDateFixRunner: updated {} PENDING contract(s) to start {} and end {}",
                    toUpdate.size(), septFirst, septFirst.plusDays(90));
        } else {
            log.info("PendingContractDateFixRunner: no updates required (all PENDING already aligned to 01/09/{})", currentYear);
        }
    }
}
