package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Blog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.BlogRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class BlogSeeder {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private UserRepository userRepository;

    public void seed() {
        if (blogRepository.count() == 0) {
            User admin = userRepository.findByEmail("admin@test.com").orElseThrow(() -> new RuntimeException("Admin user not found"));
            User manager = userRepository.findByEmail("manager@test.com").orElseThrow(() -> new RuntimeException("Manager user not found"));
            User teacher = userRepository.findByEmail("teacher@test.com").orElseThrow(() -> new RuntimeException("Teacher user not found"));
            User student = userRepository.findByEmail("student@test.com").orElseThrow(() -> new RuntimeException("Student user not found"));
            
            LocalDateTime now = LocalDateTime.now();

            Blog blog1 = new Blog();
            // Blog 1 - Published by Adminvới Nền tảng Lớp học của Chúng tôi
            blog1.setTitle("Chào mừng đến với Nền tảng Lớp học của Chúng tôi");
            blog1.setDescription(
                    "Đây là bài viết blog đầu tiên trên nền tảng lớp học của chúng tôi. Chúng tôi rất vui mừng thông báo về việc ra mắt môi trường học tập kỹ thuật số mới được thiết kế để tạo điều kiện giao tiếp tốt hơn giữa giáo viên và học sinh.\\n\\nNền tảng của chúng tôi bao gồm các tính năng như:\\n- Lớp học ảo\\n- Nộp bài tập\\n- Hệ thống chấm điểm\\n- Diễn đàn thảo luận\\n\\nChúng tôi hy vọng bạn sẽ thích sử dụng nền tảng của chúng tôi!");
            blog1.setImageUrl(
                    "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
            blog1.setThumbnailUrl(
                    "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
            blog1.setTags("thông báo, chào mừng, nền tảng");
            blog1.setAuthor(admin);
            blog1.setIsPublished(true);
            blog1.setStatus("published");
            blog1.setPublishedDate(now.minusDays(7));
            blog1.setLastEditedDate(now.minusDays(7));
            blog1.setLastEditedBy(admin);
            blog1.setViewCount(156);
            blogRepository.save(blog1);

            Blog blog2 = new Blog();
            // Blog 2 - Published by Teacherến Hiệu Quả
            blog2.setTitle("Mẹo Học Trực Tuyến Hiệu Quả");
            blog2.setDescription(
                    "Khi chúng ta chuyển sang học trực tuyến nhiều hơn, đây là một số mẹo để giúp học sinh thành công:\\n\\n1. **Tạo không gian học tập riêng** - Tìm một nơi yên tĩnh, thoải mái nơi bạn có thể tập trung.\\n\\n2. **Thiết lập thói quen** - Đặt giờ học cố định và tuân thủ chúng.\\n\\n3. **Nghỉ giải lao** - Sử dụng các kỹ thuật như phương pháp Pomodoro (25 phút làm việc sau đó nghỉ 5 phút).\\n\\n4. **Giữ tổ chức** - Sử dụng lịch kỹ thuật số và danh sách việc cần làm để theo dõi bài tập và thời hạn.\\n\\n5. **Tham gia tích cực** - Tham gia vào các cuộc thảo luận trực tuyến và đặt câu hỏi khi cần giúp đỡ.\\n\\nBạn thấy chiến lược nào hiệu quả nhất cho việc học trực tuyến? Hãy chia sẻ trong phần bình luận!");
            blog2.setImageUrl(
                    "https://images.unsplash.com/photo-1498243691581-b145c3f54a5a?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
            blog2.setThumbnailUrl(
                    "https://images.unsplash.com/photo-1498243691581-b145c3f54a5a?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
            blog2.setVideoUrl("https://www.youtube.com/embed/sBJmRD7kNTk");
            blog2.setTags("học trực tuyến, mẹo học tập, giáo dục");
            blog2.setAuthor(teacher);
            blog2.setIsPublished(true);
            blog2.setStatus("published");
            blog2.setPublishedDate(now.minusDays(5));
            blog2.setLastEditedDate(now.minusDays(5));
            blog2.setLastEditedBy(teacher);
            blog2.setViewCount(89);
            blogRepository.save(blog2);

            Blog blog3 = new Blog();
            // Blog 3 - Draft by Manageri Sắp Ra Mắt Cho Học Kỳ Tới
            blog3.setTitle("Tính Năng Mới Sắp Ra Mắt Cho Học Kỳ Tới");
            blog3.setDescription(
                    "Chúng tôi đang làm việc trên một số tính năng thú vị sẽ được phát hành trong học kỳ tới. Những cải tiến này dựa trên phản hồi từ học sinh và giáo viên.\\n\\n**Sắp Ra Mắt:**\\n\\n- Hệ thống tin nhắn cải tiến\\n- Công cụ cộng tác thời gian thực\\n- Ứng dụng di động cho iOS và Android\\n- Tích hợp với các công cụ giáo dục phổ biến\\n- Phân tích nâng cao cho giáo viên\\n\\nBài viết này vẫn đang là bản nháp và sẽ được cập nhật với nhiều chi tiết hơn trước khi xuất bản.");
            blog3.setThumbnailUrl("https://i1.sndcdn.com/artworks-000473680527-kz21lf-t1080x1080.jpg");
            blog3.setTags("tính năng, sắp ra mắt, cải tiến");
            blog3.setAuthor(manager);
            blog3.setIsPublished(false);
            blog3.setStatus("draft");
            blog3.setLastEditedDate(now.minusDays(2));
            blog3.setLastEditedBy(manager);
            blogRepository.save(blog3);

            Blog blog4 = new Blog();
            // Blog 4 - Published by Manager with image and video Tàng Thế Giới
            blog4.setTitle("Chuyến Tham Quan Ảo: Khám Phá Các Bảo Tàng Thế Giới");
            blog4.setDescription(
                    "Hôm nay chúng ta sẽ thực hiện một chuyến tham quan ảo đến một số bảo tàng nổi tiếng nhất thế giới cung cấp các tour trực tuyến.\\n\\nNhiều bảo tàng uy tín cung cấp các tour ảo cho phép bạn khám phá bộ sưu tập của họ từ sự thoải mái của ngôi nhà. Đây là một nguồn tài nguyên giáo dục tuyệt vời cho nghệ thuật, lịch sử và nghiên cứu văn hóa.\\n\\n**Các bảo tàng được giới thiệu trong video:**\\n\\n- Bảo tàng Louvre, Paris\\n- Bảo tàng Anh, London\\n- Bảo tàng Nghệ thuật Metropolitan, New York\\n- Bảo tàng Vatican, Rome\\n- Bảo tàng Nghệ thuật Hiện đại và Đương đại Quốc gia, Seoul\\n\\nVideo đính kèm cung cấp một tour có hướng dẫn của các bảo tàng này. Chúng tôi hy vọng chuyến tham quan ảo này sẽ truyền cảm hứng cho học sinh tìm hiểu thêm về nghệ thuật và lịch sử!");
            blog4.setImageUrl(
                    "https://images.unsplash.com/photo-1515169273894-7e876dcf13da?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
            blog4.setThumbnailUrl(
                    "https://images.unsplash.com/photo-1515169273894-7e876dcf13da?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
            blog4.setVideoUrl("https://www.youtube.com/embed/vQ_sAt-VzRk");
            blog4.setTags("tham quan ảo, bảo tàng, nghệ thuật, lịch sử, giáo dục");
            blog4.setAuthor(manager);
            blog4.setIsPublished(true);
            blog4.setStatus("published");
            blog4.setPublishedDate(now.minusDays(1));
            blog4.setLastEditedDate(now.minusDays(1));
            blog4.setLastEditedBy(manager);
            blog4.setViewCount(42);
            blogRepository.save(blog4);

            Blog blog5 = new Blog();
            // Blog 5 - Published by Student (if they can create blogs)t Đầu Đến Nâng Cao
            blog5.setTitle("Hành Trình Học Tập Của Tôi: Từ Người Mới Bắt Đầu Đến Nâng Cao");
            blog5.setDescription(
                    "Xin chào mọi người! Tôi muốn chia sẻ hành trình học tập cá nhân của mình và một số hiểu biết có thể giúp ích cho các học sinh khác.\\n\\n"
                            +
                            "**Trải Nghiệm Của Tôi:**\\n\\n" +
                            "Khi tôi lần đầu sử dụng nền tảng này, tôi cảm thấy choáng ngợp bởi tất cả các tính năng. Nhưng dần dần, tôi khám phá ra cách mỗi công cụ có thể giúp tôi học tốt hơn.\\n\\n"
                            +
                            "**Những Bài Học Chính:**\\n\\n" +
                            "1. **Tính nhất quán là chìa khóa** - Học một chút mỗi ngày tốt hơn là nhồi nhét\\n" +
                            "2. **Đặt câu hỏi** - Đừng ngần ngại hỏi giáo viên hoặc bạn học để được giúp đỡ\\n" +
                            "3. **Sử dụng tất cả tài nguyên** - Tận dụng bài giảng, bài tập và diễn đàn thảo luận\\n" +
                            "4. **Theo dõi tiến độ** - Giám sát kết quả học tập để xác định các lĩnh vực cần cải thiện\\n" +
                            "5. **Kết nối** - Tham gia vào cộng đồng thông qua tin nhắn và thông báo\\n\\n" +
                            "**Lời Khuyên Cho Học Sinh Mới:**\\n\\n" +
                            "- Bắt đầu với những điều cơ bản và dần dần khám phá các tính năng nâng cao\\n" +
                            "- Thiết lập lịch học phù hợp với bạn\\n" +
                            "- Đừng sợ mắc lỗi - đó là cách chúng ta học!\\n" +
                            "- Kết nối với các học sinh khác có cùng sở thích\\n\\n" +
                            "Tôi hy vọng điều này sẽ giúp ích cho ai đó trong hành trình học tập của họ!");
            blog5.setImageUrl(
                    "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
            blog5.setThumbnailUrl(
                    "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
            blog5.setTags("trải nghiệm học sinh, mẹo học tập, động lực, cộng đồng");
            blog5.setAuthor(student); // Student user
            blog5.setIsPublished(true);
            blog5.setStatus("published");
            blog5.setPublishedDate(now.minusHours(12));
            blog5.setLastEditedDate(now.minusHours(12));
            blog5.setLastEditedBy(student);
            blog5.setViewCount(23);
            blogRepository.save(blog5);
            
            System.out.println("✅ [BlogSeeder] Created 5 sample blogs");
        }
    }
} 