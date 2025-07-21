package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.StudentMessage;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.StudentMessageRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class MessageSeeder {

    private final StudentMessageRepository studentMessageRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public MessageSeeder(StudentMessageRepository studentMessageRepository, UserRepository userRepository) {
        this.studentMessageRepository = studentMessageRepository;
        this.userRepository = userRepository;
    }

    public void seed() {
        if (studentMessageRepository.count() > 0) {
            System.out.println("✅ [MessageSeeder] Messages already seeded.");
            return;
        }

        System.out.println("🔄 [MessageSeeder] Seeding messages...");

        List<User> students = userRepository.findByRoleId(1); // STUDENT
        List<User> teachers = userRepository.findByRoleId(2); // TEACHER
        List<User> managers = userRepository.findByRoleId(3); // MANAGER

        System.out.println("📊 [MessageSeeder] Found " + students.size() + " students, " + teachers.size() + " teachers, " + managers.size() + " managers");

        if (students.isEmpty()) {
            System.out.println("⚠️ [MessageSeeder] No students found. Skipping message seeding.");
            return;
        }

        if (teachers.isEmpty()) {
            System.out.println("⚠️ [MessageSeeder] No teachers found. Skipping message seeding.");
            return;
        }

        int messageCount = 0;

        // Create conversations for EACH student with at least one teacher
        for (User student : students) {
            // Each student gets 2-3 conversations with different teachers
            int conversationsPerStudent = 2 + random.nextInt(2); // 2-3 conversations
            for (int i = 0; i < Math.min(conversationsPerStudent, teachers.size()); i++) {
                User teacher = teachers.get(i % teachers.size());
                createConversation(student, teacher);
                messageCount += 2;
            }
        }

        // Create some requests to managers if managers exist
        if (!managers.isEmpty()) {
            User studentForRequest = students.get(0);
            User manager = managers.get(0);
            createMessage(studentForRequest, manager, "Thắc mắc về học phí", "Em chào thầy/cô, em muốn hỏi về chính sách học phí cho học kỳ tới ạ.", "MEDIUM", true);
            messageCount++;

            if (!teachers.isEmpty()) {
                User teacherForRequest = teachers.get(0);
                createMessage(teacherForRequest, manager, "Đề xuất cải tiến giáo trình", "Tôi có một vài ý tưởng để cải tiến nội dung giáo trình môn học, rất mong được trao đổi với ban quản lý.", "LOW", false);
                messageCount++;
            }
        }

        System.out.println("✅ [MessageSeeder] Created " + messageCount + " sample messages for " + students.size() + " students.");
    }

    private void createConversation(User student, User teacher) {
        String[] subjects = {"Thắc mắc về bài tập", "Xin phép nghỉ học", "Hỏi về điểm số", "Cần tư vấn thêm"};
        String[] studentMessages = {
            "Em có một vài câu hỏi về bài tập tuần này ạ.",
            "Em viết email này để xin phép nghỉ buổi học tới do có việc gia đình.",
            "Thầy/cô có thể xem lại giúp em điểm bài kiểm tra vừa rồi không ạ?",
            "Em đang gặp chút khó khăn với nội dung môn học, thầy/cô có thể cho em một buổi tư vấn được không?"
        };
        String[] teacherReplies = {
            "Chào em, em cứ hỏi nhé, thầy/cô sẽ giải đáp.",
            "Thầy/cô đã nhận được thông tin. Em nhớ xem lại bài giảng nhé.",
            "Được em, thầy/cô sẽ kiểm tra lại và phản hồi sớm.",
            "Chắc chắn rồi, em có thể ghé văn phòng thầy/cô vào chiều thứ 5 nhé."
        };

        String subject = subjects[random.nextInt(subjects.length)];
        String studentMessage = studentMessages[random.nextInt(studentMessages.length)];
        String teacherReply = teacherReplies[random.nextInt(teacherReplies.length)];

        // Student sends first message
        createMessage(student, teacher, subject, studentMessage, "MEDIUM", true);

        // Teacher replies
        createMessage(teacher, student, "Re: " + subject, teacherReply, "MEDIUM", random.nextBoolean());
    }

    private StudentMessage createMessage(User sender, User recipient, String subject, String content, String priority, boolean isRead) {
        StudentMessage message = new StudentMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setSubject(subject);
        message.setContent(content);
        message.setPriority(priority);
        message.setStatus("DELIVERED");
        message.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(10)).minusHours(random.nextInt(24)));
        message.setIsRead(isRead);
        return studentMessageRepository.save(message);
    }
} 