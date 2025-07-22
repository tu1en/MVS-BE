package com.classroomapp.classroombackend.config;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.model.usermanagement.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class ContractDataLoader implements CommandLineRunner {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only load data if contracts table is empty
        if (contractRepository.count() == 0) {
            loadSampleContracts();
        }
    }

    private void loadSampleContracts() {
        // Get all users to create contracts for
        List<User> users = userRepository.findAll();
        
        if (users.isEmpty()) {
            System.out.println("No users found. Cannot create sample contracts.");
            return;
        }

        String[] contractTypes = {"Chính thức", "Thử việc", "Thời vụ", "Bán thời gian", "Hợp đồng có thời hạn"};
        String[] positions = {"Giáo viên Toán", "Giáo viên Văn", "Giáo viên Anh", "Giáo viên Lý", "Giáo viên Hóa", 
                             "Giáo viên Sinh", "Giáo viên Sử", "Giáo viên Địa", "Kế toán viên", "Quản lý", "Thư ký", "Bảo vệ"};
        String[] departments = {"Khối Tiểu học", "Khối THCS", "Khối THPT", "Phòng Kế toán", "Phòng Hành chính", "Ban Giám hiệu"};
        String[] statuses = {"ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "TERMINATED", "EXPIRED"};
        String[] workingHours = {"8:00-17:00", "7:30-16:30", "8:30-17:30", "13:00-22:00", "6:00-15:00"};
        
        Random random = new Random();
        
        // Create 15 sample contracts with unique user IDs
        for (int i = 0; i < Math.min(15, users.size()); i++) {
            User user = users.get(i);
            
            Contract contract = new Contract();
            contract.setUserId(user.getId());
            contract.setFullName(user.getFullName());
            contract.setContractType(contractTypes[random.nextInt(contractTypes.length)]);
            contract.setPosition(positions[random.nextInt(positions.length)]);
            contract.setDepartment(departments[random.nextInt(departments.length)]);
            
            // Random salary between 8,000,000 and 25,000,000 VND
            double salary = 8000000 + (random.nextDouble() * 17000000);
            contract.setSalary(salary);
            
            contract.setWorkingHours(workingHours[random.nextInt(workingHours.length)]);
            
            // Random start date within last 2 years
            LocalDate startDate = LocalDate.now().minusDays(random.nextInt(730));
            contract.setStartDate(startDate);
            
            // Some contracts have end dates
            if (random.nextBoolean()) {
                contract.setEndDate(startDate.plusYears(1 + random.nextInt(3)));
            }
            
            contract.setStatus(statuses[random.nextInt(statuses.length)]);
            contract.setCreatedBy("admin@system.com");
            contract.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            
            // Add termination info for terminated contracts
            if ("TERMINATED".equals(contract.getStatus())) {
                contract.setTerminationReason("Hết hạn hợp đồng");
                contract.setTerminationDate(contract.getStartDate().plusMonths(6 + random.nextInt(18)));
                contract.setWhoApproved("Giám đốc Nguyễn Văn A");
                contract.setSettlementInfo("Đã thanh toán đầy đủ lương và phụ cấp");
            }
            
            // Add some notes
            String[] notes = {
                "Nhân viên có kinh nghiệm tốt, làm việc chăm chỉ",
                "Cần theo dõi hiệu suất làm việc trong thời gian thử việc",
                "Nhân viên có chuyên môn cao, phù hợp với vị trí",
                "Đã hoàn thành đầy đủ các thủ tục nhập việc",
                "Cần bổ sung thêm đào tạo về quy trình làm việc"
            };
            contract.setNotes(notes[random.nextInt(notes.length)]);
            
            contractRepository.save(contract);
        }
        
        System.out.println("Sample contract data loaded successfully!");
    }
}
