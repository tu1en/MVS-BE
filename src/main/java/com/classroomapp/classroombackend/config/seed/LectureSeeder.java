package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.LectureMaterial;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.LectureMaterialRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;

@Component
public class LectureSeeder {

    @Autowired
    private LectureRepository lectureRepository;
    
    @Autowired
    private LectureMaterialRepository lectureMaterialRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    public void seed(List<Classroom> classrooms) {
        if (lectureRepository.count() == 0) {
            
            if (classrooms.isEmpty()) {
                System.out.println("⚠️ [LectureSeeder] No classrooms found. Skipping.");
                return;
            }
            
            // Find specific classes by name (partial match)
            Classroom mathClass = findClassroomByPartialName(classrooms, "Toán");
            Classroom litClass = findClassroomByPartialName(classrooms, "Văn học Việt Nam");
            Classroom engClass = findClassroomByPartialName(classrooms, "Tiếng Anh");
            Classroom csClass = findClassroomByPartialName(classrooms, "Công nghệ thông tin");
            
            // Create Math lectures
            if (mathClass != null) {
                // Lecture 1
                Lecture mathLecture1 = new Lecture();
                mathLecture1.setTitle("Giới thiệu về Đạo hàm");
                mathLecture1.setContent("# Giới thiệu về Đạo hàm\n\n## Định nghĩa đạo hàm\n\nĐạo hàm của một hàm số f(x) tại điểm x₀, ký hiệu là f'(x₀), được định nghĩa là:\n\nf'(x₀) = lim(h→0) [f(x₀+h) - f(x₀)]/h\n\nĐạo hàm cho ta biết tốc độ biến thiên của hàm số tại một điểm.\n\n## Các quy tắc tính đạo hàm\n\n1. Đạo hàm của hằng số: (C)' = 0\n2. Đạo hàm của hàm lũy thừa: (x^n)' = n*x^(n-1)\n3. Đạo hàm của tổng: (f+g)' = f' + g'\n4. Đạo hàm của tích: (f*g)' = f'*g + f*g'\n5. Đạo hàm của thương: (f/g)' = (f'*g - f*g')/g²\n\n## Ứng dụng của đạo hàm\n\n- Tìm tiếp tuyến của đường cong\n- Tìm cực trị của hàm số\n- Tìm giá trị lớn nhất, nhỏ nhất của hàm số\n- Giải các bài toán tối ưu");
                mathLecture1.setClassroom(mathClass);
                lectureRepository.save(mathLecture1);
                
                // Lecture 1 Material
                LectureMaterial mathMaterial1 = new LectureMaterial();
                mathMaterial1.setFileName("dao_ham_va_ung_dung.pdf");
                mathMaterial1.setContentType("application/pdf");
                mathMaterial1.setDownloadUrl("https://firebasestorage.googleapis.com/v0/b/sep490-e5896.appspot.com/o/materials%2Fdao_ham_va_ung_dung.pdf");
                mathMaterial1.setFilePath("/materials/dao_ham_va_ung_dung.pdf");
                mathMaterial1.setFileSize(1245678L);
                mathMaterial1.setLecture(mathLecture1);
                lectureMaterialRepository.save(mathMaterial1);
                
                // Lecture 2
                Lecture mathLecture2 = new Lecture();
                mathLecture2.setTitle("Giới thiệu về Tích phân");
                mathLecture2.setContent("# Giới thiệu về Tích phân\n\n## Định nghĩa tích phân\n\nTích phân không xác định của hàm số f(x), ký hiệu là ∫f(x)dx, là một hàm số F(x) sao cho F'(x) = f(x).\n\nTích phân xác định của hàm số f(x) trên đoạn [a,b], ký hiệu là ∫(a→b)f(x)dx, được định nghĩa là:\n\n∫(a→b)f(x)dx = F(b) - F(a)\n\ntrong đó F(x) là một nguyên hàm của f(x).\n\n## Các phương pháp tính tích phân\n\n1. Tích phân từng phần: ∫u(x)v'(x)dx = u(x)v(x) - ∫u'(x)v(x)dx\n2. Phương pháp đổi biến\n3. Phương pháp phân tích thành phân số đơn giản\n\n## Ứng dụng của tích phân\n\n- Tính diện tích hình phẳng\n- Tính thể tích vật thể\n- Tính độ dài cung\n- Tính công, năng lượng trong vật lý\n- Xác suất và thống kê");
                mathLecture2.setClassroom(mathClass);
                lectureRepository.save(mathLecture2);
                
                // Lecture 2 Video Material
                LectureMaterial mathMaterial2 = new LectureMaterial();
                mathMaterial2.setFileName("huong_dan_tinh_tich_phan.mp4");
                mathMaterial2.setContentType("video/mp4");
                mathMaterial2.setDownloadUrl("https://firebasestorage.googleapis.com/v0/b/sep490-e5896.appspot.com/o/materials%2Fhuong_dan_tinh_tich_phan.mp4");
                mathMaterial2.setFilePath("/materials/huong_dan_tinh_tich_phan.mp4");
                mathMaterial2.setFileSize(15678932L);
                mathMaterial2.setLecture(mathLecture2);
                lectureMaterialRepository.save(mathMaterial2);
                
                System.out.println("✅ [LectureSeeder] Created 2 lectures with materials for Math class");
            }
            
            // Create Literature lectures
            if (litClass != null) {
                // Lecture 1
                Lecture litLecture1 = new Lecture();
                litLecture1.setTitle("Phân tích tác phẩm Truyện Kiều");
                litLecture1.setContent("# Phân tích tác phẩm Truyện Kiều\n\n## Giới thiệu về tác giả và tác phẩm\n\nNguyễn Du (1766-1820), danh sĩ thời Lê mạt - Nguyễn sơ, là tác giả của kiệt tác \"Truyện Kiều\" (Đoạn trường tân thanh).\n\nTruyện Kiều là một tác phẩm văn học lớn của dân tộc, gồm 3254 câu thơ lục bát, viết theo thể song thất lục bát.\n\n## Nội dung cốt truyện\n\nTruyện kể về cuộc đời của Thúy Kiều, một người con gái tài sắc vẹn toàn nhưng gặp nhiều bất hạnh trong cuộc đời. Vì hiếu thảo với cha mẹ, nàng phải bán mình chuộc cha và trải qua 15 năm lưu lạc với nhiều đau khổ. Cuối cùng, nàng đoàn tụ với người yêu đầu tiên là Kim Trọng.\n\n## Các giá trị của tác phẩm\n\n1. Giá trị nhân đạo: Tố cáo xã hội phong kiến, đề cao quyền sống và hạnh phúc của con người.\n2. Giá trị tư tưởng: Quan niệm về số phận, nhân quả, thuyết \"tài mệnh tương đố\".\n3. Giá trị nghệ thuật: Ngôn ngữ thơ tinh tế, hình ảnh đẹp, tính dân tộc cao.\n\n## Nhân vật Thúy Kiều\n\nThúy Kiều là một nhân vật bi kịch điển hình trong văn học Việt Nam. Nàng đại diện cho vẻ đẹp tài sắc, sự hiếu thảo và số phận bi thương của người phụ nữ trong xã hội phong kiến.");
                litLecture1.setClassroom(litClass);
                lectureRepository.save(litLecture1);
                
                // Lecture 1 Material
                LectureMaterial litMaterial1 = new LectureMaterial();
                litMaterial1.setFileName("truyen_kieu_phan_tich.docx");
                litMaterial1.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                litMaterial1.setDownloadUrl("https://firebasestorage.googleapis.com/v0/b/sep490-e5896.appspot.com/o/materials%2Ftruyen_kieu_phan_tich.docx");
                litMaterial1.setFilePath("/materials/truyen_kieu_phan_tich.docx");
                litMaterial1.setFileSize(546789L);
                litMaterial1.setLecture(litLecture1);
                lectureMaterialRepository.save(litMaterial1);
                
                System.out.println("✅ [LectureSeeder] Created 1 lecture with materials for Literature class");
            }
            
            // Create English lectures
            if (engClass != null) {
                // Lecture 1
                Lecture engLecture1 = new Lecture();
                engLecture1.setTitle("Introduction to Academic Writing");
                engLecture1.setContent("# Introduction to Academic Writing\n\n## What is Academic Writing?\n\nAcademic writing is a formal style of writing used in universities and scholarly publications. It has specific conventions in terms of structure, style, and tone.\n\n## Key Characteristics of Academic Writing\n\n1. **Formal tone**: Avoids colloquial language, contractions, and first-person perspective (in some disciplines).\n2. **Clear structure**: Includes introduction, body paragraphs, and conclusion.\n3. **Evidence-based**: Supports claims with evidence from reliable sources.\n4. **Precise language**: Uses specific terminology and avoids ambiguity.\n5. **Objective stance**: Presents balanced arguments and acknowledges different perspectives.\n\n## The Writing Process\n\n1. **Planning**: Understand the assignment, brainstorm ideas, and create an outline.\n2. **Drafting**: Write your first draft focusing on content rather than perfection.\n3. **Revising**: Restructure, rewrite, and refine your arguments.\n4. **Editing**: Check for clarity, coherence, and flow.\n5. **Proofreading**: Correct grammar, spelling, and punctuation errors.\n\n## Citation Styles\n\nDifferent disciplines use different citation styles:\n- APA (American Psychological Association): Used in social sciences\n- MLA (Modern Language Association): Used in humanities\n- Chicago: Used in history and some humanities\n- Harvard: Used across many disciplines\n\n## Common Mistakes to Avoid\n\n- Overgeneralization\n- Personal opinions without evidence\n- Informal language\n- Plagiarism\n- Wordiness");
                engLecture1.setClassroom(engClass);
                lectureRepository.save(engLecture1);
                
                // Lecture 1 Material
                LectureMaterial engMaterial1 = new LectureMaterial();
                engMaterial1.setFileName("academic_writing_guide.pdf");
                engMaterial1.setContentType("application/pdf");
                engMaterial1.setDownloadUrl("https://firebasestorage.googleapis.com/v0/b/sep490-e5896.appspot.com/o/materials%2Facademic_writing_guide.pdf");
                engMaterial1.setFilePath("/materials/academic_writing_guide.pdf");
                engMaterial1.setFileSize(987654L);
                engMaterial1.setLecture(engLecture1);
                lectureMaterialRepository.save(engMaterial1);
                
                // Lecture 2
                Lecture engLecture2 = new Lecture();
                engLecture2.setTitle("Effective Public Speaking");
                engLecture2.setContent("# Effective Public Speaking\n\n## The Importance of Public Speaking\n\nPublic speaking is a critical skill in both academic and professional environments. It helps you communicate your ideas clearly, build confidence, and influence others.\n\n## Preparing Your Speech\n\n1. **Understand your audience**: Consider their knowledge level, interests, and expectations.\n2. **Define your purpose**: Are you informing, persuading, or entertaining?\n3. **Research your topic**: Gather relevant information and supporting evidence.\n4. **Structure your speech**: Create a clear introduction, body, and conclusion.\n5. **Prepare visual aids**: Use slides or props to enhance understanding.\n\n## Delivering Your Speech\n\n1. **Body language**: Maintain good posture, use appropriate gestures, and make eye contact.\n2. **Voice modulation**: Vary your pace, volume, and tone to keep the audience engaged.\n3. **Manage nervousness**: Practice deep breathing, visualize success, and prepare thoroughly.\n4. **Engage the audience**: Ask questions, include relevant anecdotes, and respond to feedback.\n\n## Common Pitfalls to Avoid\n\n- Reading directly from notes\n- Speaking too quickly or monotonously\n- Using too many filler words (um, uh, like)\n- Overloading slides with text\n- Ignoring time limits\n\n## Practice Tips\n\n- Record yourself and analyze your performance\n- Practice in front of friends or family\n- Join a public speaking club\n- Seek constructive feedback");
                engLecture2.setClassroom(engClass);
                lectureRepository.save(engLecture2);
                
                // Lecture 2 Video Material
                LectureMaterial engMaterial2 = new LectureMaterial();
                engMaterial2.setFileName("public_speaking_examples.mp4");
                engMaterial2.setContentType("video/mp4");
                engMaterial2.setDownloadUrl("https://firebasestorage.googleapis.com/v0/b/sep490-e5896.appspot.com/o/materials%2Fpublic_speaking_examples.mp4");
                engMaterial2.setFilePath("/materials/public_speaking_examples.mp4");
                engMaterial2.setFileSize(25674123L);
                engMaterial2.setLecture(engLecture2);
                lectureMaterialRepository.save(engMaterial2);
                
                System.out.println("✅ [LectureSeeder] Created 2 lectures with materials for English class");
            }
            
            // Create CS lectures
            if (csClass != null) {
                // Lecture 1
                Lecture csLecture1 = new Lecture();
                csLecture1.setTitle("Giới thiệu về lập trình Java cơ bản");
                csLecture1.setContent("# Giới thiệu về lập trình Java cơ bản\n\n## Java là gì?\n\nJava là một ngôn ngữ lập trình hướng đối tượng, độc lập nền tảng, được phát triển bởi Sun Microsystems (nay là Oracle) vào năm 1995. Java có cú pháp tương tự C++ nhưng đơn giản hơn và loại bỏ các tính năng gây nhầm lẫn.\n\n## Đặc điểm của Java\n\n1. **Hướng đối tượng**: Tất cả mọi thứ trong Java đều là đối tượng.\n2. **Độc lập nền tảng**: Chương trình Java có thể chạy trên mọi hệ điều hành thông qua Java Virtual Machine (JVM).\n3. **An toàn**: Java có nhiều cơ chế bảo mật như quản lý bộ nhớ tự động, kiểm tra kiểu dữ liệu nghiêm ngặt.\n4. **Đa luồng**: Hỗ trợ lập trình đa luồng cho ứng dụng hiệu suất cao.\n\n## Cấu trúc cơ bản của chương trình Java\n\n```java\npublic class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}\n```\n\n## Các kiểu dữ liệu trong Java\n\n1. **Kiểu nguyên thủy**: int, float, double, boolean, char, byte, short, long\n2. **Kiểu tham chiếu**: classes, interfaces, arrays\n\n## Cấu trúc điều khiển\n\n1. **Cấu trúc rẽ nhánh**: if-else, switch\n2. **Cấu trúc lặp**: for, while, do-while\n3. **Cấu trúc nhảy**: break, continue, return\n\n## Lập trình hướng đối tượng trong Java\n\n1. **Class và Object**\n2. **Kế thừa**\n3. **Đa hình**\n4. **Trừu tượng**\n5. **Đóng gói**");
                csLecture1.setClassroom(csClass);
                lectureRepository.save(csLecture1);
                
                // Lecture 1 Material
                LectureMaterial csMaterial1 = new LectureMaterial();
                csMaterial1.setFileName("java_fundamentals.pdf");
                csMaterial1.setContentType("application/pdf");
                csMaterial1.setDownloadUrl("https://firebasestorage.googleapis.com/v0/b/sep490-e5896.appspot.com/o/materials%2Fjava_fundamentals.pdf");
                csMaterial1.setFilePath("/materials/java_fundamentals.pdf");
                csMaterial1.setFileSize(1456789L);
                csMaterial1.setLecture(csLecture1);
                lectureMaterialRepository.save(csMaterial1);
                
                // Lecture 1 Code Examples
                LectureMaterial csMaterial2 = new LectureMaterial();
                csMaterial2.setFileName("java_examples.zip");
                csMaterial2.setContentType("application/zip");
                csMaterial2.setDownloadUrl("https://firebasestorage.googleapis.com/v0/b/sep490-e5896.appspot.com/o/materials%2Fjava_examples.zip");
                csMaterial2.setFilePath("/materials/java_examples.zip");
                csMaterial2.setFileSize(2345678L);
                csMaterial2.setLecture(csLecture1);
                lectureMaterialRepository.save(csMaterial2);
                
                System.out.println("✅ [LectureSeeder] Created 1 lecture with materials for CS class");
            }
            
            System.out.println("✅ [LectureSeeder] Created lectures and materials for available classes");
        } else {
            System.out.println("✅ [LectureSeeder] Lectures already seeded");
        }
    }
    
    private Classroom findClassroomByPartialName(List<Classroom> classrooms, String partialName) {
        return classrooms.stream()
                .filter(c -> c.getName().contains(partialName))
                .findFirst()
                .orElse(null);
    }
} 