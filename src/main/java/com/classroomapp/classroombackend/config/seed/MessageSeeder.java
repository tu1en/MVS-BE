package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.StudentMessage;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.StudentMessageRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class MessageSeeder {

    private final StudentMessageRepository studentMessageRepository;
    private final UserRepository userRepository;

    public MessageSeeder(StudentMessageRepository studentMessageRepository, UserRepository userRepository) {
        this.studentMessageRepository = studentMessageRepository;
        this.userRepository = userRepository;
    }

    public void seed() {
        if (studentMessageRepository.count() > 0) {
            return;
        }

        try {
            User student1 = userRepository.findByEmail("student1@test.com").orElse(null);
            User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
            User manager = userRepository.findByEmail("manager@test.com").orElse(null);
            User admin = userRepository.findByEmail("admin@test.com").orElse(null);
            User student2 = userRepository.findByEmail("student2@test.com").orElse(null);
            User student3 = userRepository.findByEmail("student3@test.com").orElse(null);

            if (student1 == null || teacher == null || manager == null || admin == null || student2 == null || student3 == null) {
                System.out.println("⚠️ [MessageSeeder] Not all required users found. Skipping message seeding.");
                return;
            }

            // --- Conversation 1: Teacher and Student1 ---
            StudentMessage msg1 = new StudentMessage();
            msg1.setSender(student1);
            msg1.setRecipient(teacher);
            msg1.setSubject("Question about Math Assignment");
            msg1.setContent("Hi Teacher, I have a question about the math assignment due next week. Could you please clarify the requirements for problem 3?");
            msg1.setPriority("MEDIUM");
            msg1.setStatus("DELIVERED");
            studentMessageRepository.save(msg1);

            StudentMessage msg2 = new StudentMessage();
            msg2.setSender(teacher);
            msg2.setRecipient(student1);
            msg2.setSubject("Re: Question about Math Assignment");
            msg2.setContent("Hello! For problem 3, please make sure to show all your work step by step. Focus on the algebraic manipulation we covered in class last Tuesday.");
            msg2.setPriority("MEDIUM");
            msg2.setStatus("DELIVERED");
            studentMessageRepository.save(msg2);

            // --- Conversation 2: Teacher and Student2 ---
            StudentMessage msg4 = new StudentMessage();
            msg4.setSender(student2);
            msg4.setRecipient(teacher);
            msg4.setSubject("Schedule Change Request");
            msg4.setContent("Dear Teacher, I won't be able to attend the class this Friday due to a medical appointment. Is there any makeup session available?");
            msg4.setPriority("HIGH");
            msg4.setStatus("DELIVERED");
            studentMessageRepository.save(msg4);

            // --- Conversation 3: Teacher and Student3 ---
             StudentMessage msg6 = new StudentMessage();
             msg6.setSender(teacher);
             msg6.setRecipient(student3);
             msg6.setSubject("Test Reminder");
             msg6.setContent("This is a reminder that we have our midterm test next Monday. Please review chapters 1-5. Good luck!");
             msg6.setPriority("HIGH");
             msg6.setStatus("DELIVERED");
             studentMessageRepository.save(msg6);

            // --- Messages to Manager ---
            StudentMessage msgManager1 = new StudentMessage();
            msgManager1.setSender(teacher);
            msgManager1.setRecipient(manager);
            msgManager1.setSubject("Yêu cầu hỗ trợ về lịch dạy");
            msgManager1.setContent("Kính gửi Ban quản lý,\n\nTôi cần được hỗ trợ về việc điều chỉnh lịch dạy tuần tới do có công việc đột xuất. Tôi phải tham gia một hội thảo vào thứ Ba tuần sau. Mong Ban quản lý xem xét và sắp xếp lại lịch dạy giúp tôi.\n\nTrân trọng cảm ơn.");
            msgManager1.setPriority("HIGH");
            msgManager1.setStatus("DELIVERED");
            msgManager1.setIsRead(false);
            msgManager1.setCreatedAt(LocalDateTime.now().minusDays(1));
            studentMessageRepository.save(msgManager1);

            StudentMessage msgManager2 = new StudentMessage();
            msgManager2.setSender(student1);
            msgManager2.setRecipient(manager);
            msgManager2.setSubject("Thắc mắc về học phí");
            msgManager2.setContent("Kính gửi Ban quản lý,\n\nEm muốn hỏi thông tin về chính sách học phí kỳ tới. Em có được giảm học phí không nếu em đăng ký nhiều khóa học cùng lúc? Và thời hạn đóng học phí là khi nào ạ?\n\nEm xin cảm ơn.");
            msgManager2.setPriority("MEDIUM");
            msgManager2.setStatus("DELIVERED");
            msgManager2.setIsRead(true);
            msgManager2.setCreatedAt(LocalDateTime.now().minusDays(2));
            studentMessageRepository.save(msgManager2);

            StudentMessage msgManager3 = new StudentMessage();
            msgManager3.setSender(admin);
            msgManager3.setRecipient(manager);
            msgManager3.setSubject("Cập nhật chính sách mới");
            msgManager3.setContent("Thông báo về việc cập nhật chính sách đánh giá giảng viên. Từ tháng sau, chúng ta sẽ áp dụng quy trình đánh giá mới cho tất cả giảng viên. Vui lòng chuẩn bị các tài liệu liên quan và thông báo cho các giảng viên trong phòng ban của bạn.");
            msgManager3.setPriority("HIGH");
            msgManager3.setStatus("DELIVERED");
            msgManager3.setIsRead(true);
            msgManager3.setCreatedAt(LocalDateTime.now().minusDays(3));
            studentMessageRepository.save(msgManager3);

            System.out.println("✅ [MessageSeeder] Created sample messages between users and for the manager.");

        } catch (Exception e) {
            System.err.println("❌ Error in MessageSeeder: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 