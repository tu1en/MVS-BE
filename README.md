# Ứng Dụng Quản Lý Lớp Học - Backend

## Tổng Quan Dự Án

Đây là phần backend cho ứng dụng Quản Lý Lớp Học được xây dựng bằng Spring Boot. Ứng dụng cung cấp API cho việc quản lý người dùng, lớp học, bài tập và bài nộp trong môi trường giáo dục.

## Cấu Trúc Mã Nguồn

Dự án tuân theo cấu trúc tiêu chuẩn của ứng dụng Spring Boot:

### Cấu Trúc Thư Mục

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── classroomapp/
│   │           └── classroombackend/
│   │               ├── config/         # Các lớp cấu hình
│   │               ├── controller/     # Các endpoint API REST
│   │               ├── dto/            # Đối tượng truyền dữ liệu
│   │               ├── exception/      # Xử lý ngoại lệ tùy chỉnh
│   │               ├── model/          # Các entity ánh xạ với bảng trong DB
│   │               ├── repository/     # Giao diện truy cập dữ liệu
│   │               ├── service/        # Xử lý logic nghiệp vụ
│   │               ├── util/           # Các lớp tiện ích
│   │               └── ClassroomBackendApplication.java # Lớp chính
│   └── resources/
│       ├── application.properties      # Cấu hình ứng dụng
│       ├── static/                     # Tài nguyên tĩnh
│       └── templates/                  # Các template
└── test/                               # Mã nguồn kiểm thử
```

### Các Thành Phần Chính

1. **Entity (model):**
   - User: Người dùng (giáo viên, học sinh)
   - Classroom: Lớp học
   - Assignment: Bài tập
   - Submission: Bài nộp

2. **Kiến Trúc Ứng Dụng:**
   - Controller: Xử lý request HTTP
   - Service: Chứa logic nghiệp vụ
   - Repository: Tương tác với cơ sở dữ liệu
   - DTO: Đối tượng truyền dữ liệu giữa các tầng

## Phân Tích Mã Nguồn

### Đã Triển Khai:

1. **Tính Năng Cốt Lõi:**
   - Quản lý người dùng (đăng ký, đăng nhập)
   - Quản lý lớp học (tạo, cập nhật, xóa, ghi danh)
   - Quản lý bài tập (tạo, cập nhật, xóa)
   - Quản lý bài nộp (nộp bài, chấm điểm, phản hồi)

2. **Controller:**
   - GreetingController: API endpoint chào mừng cơ bản (/api/v1/greetings/hello)

3. **Cấu Hình:**
   - H2 Database (cơ sở dữ liệu trong bộ nhớ)
   - JPA/Hibernate cho ORM
   - Spring Security (tạm thời vô hiệu hóa)
   - Chạy trên cổng 8088

### Còn Thiếu:

1. **Bảo Mật:**
   - Xác thực dựa trên JWT
   - Phân quyền dựa trên vai trò
   - Cấu hình CORS cho tích hợp frontend

2. **Cơ Sở Dữ Liệu:**
   - Cấu hình cơ sở dữ liệu sản phẩm (MySQL/PostgreSQL)
   - Script di chuyển dữ liệu (Flyway/Liquibase)

3. **API:**
   - Phân trang cho các endpoint danh sách
   - Lọc và sắp xếp
   - Tài liệu Swagger/OpenAPI

4. **Kiểm Thử:**
   - Unit test cho tầng service
   - Integration test cho controller
   - Cấu hình cơ sở dữ liệu cho kiểm thử

## Vấn Đề Hiện Tại

1. **Vấn Đề Maven:**
   - Khó khăn trong việc giải quyết các phụ thuộc Maven
   - SimpleGreetingApp.java được tạo ra như một giải pháp tạm thời

2. **Chưa Hoàn Thiện:**
   - Dịch vụ tải lên tệp
   - Hệ thống thông báo
   - Xác thực và phân quyền
   - Cấu hình cơ sở dữ liệu sản phẩm

## Quy Ước Mã Nguồn

1. **Quy Ước Đặt Tên:**
   - Tên lớp: PascalCase (ví dụ: UserController)
   - Tên phương thức: Theo mẫu DoSomething (ví dụ: FindAllUsers)
   - Tên biến: camelCase (ví dụ: userName)
   - Hằng số: ALL_CAPS (ví dụ: MAX_USERS)

2. **Cách Tiếp Cận:**
   - Không sử dụng biến toàn cục
   - Phải có comment cho vòng lặp, biểu thức chính quy, điều kiện
   - Tên biến phải rõ ràng
   - Tên phương thức theo kiểu DoSomething để thể hiện chức năng
   - Tên lớp phải đại diện cho các chức năng bên trong lớp

## Cài Đặt & Chạy

### Yêu Cầu:
- Java 17 trở lên
- Maven

### Các Bước Chạy:
1. Clone repository
2. Di chuyển đến thư mục gốc của dự án
3. Chạy ứng dụng bằng Maven:
   ```
   mvn spring-boot:run
   ```
4. Ứng dụng sẽ khởi chạy trên cổng 8088

### Truy Cập H2 Database Console:
- URL: http://localhost:8088/h2-console
- JDBC URL: jdbc:h2:mem:classroomdb
- Username: sa
- Password: (để trống)

### Sử Dụng SimpleGreetingApp (Nếu Cần):
1. Biên dịch: `javac SimpleGreetingApp.java`
2. Chạy: `java SimpleGreetingApp`
3. Truy cập: http://localhost:8090/

## Bước Tiếp Theo

1. **Khắc Phục Vấn Đề Maven:**
   - Xóa bộ nhớ cache local
   - Cấu hình mạng
   - Sử dụng thư viện cục bộ

2. **Hoàn Thiện Triển Khai:**
   - Dịch vụ tải lên tệp
   - Hệ thống thông báo

3. **Triển Khai Bảo Mật:**
   - Xác thực JWT
   - Phân quyền dựa trên vai trò

4. **Cấu Hình Cơ Sở Dữ Liệu:**
   - Kết nối đến MySQL/PostgreSQL
   - Script di chuyển dữ liệu với Flyway

## Lưu Ý Quan Trọng

1. Luôn tuân theo quy ước đặt tên và cách tiếp cận đã nêu trong CONVENTIONS.md.
2. Khi viết mã, hãy luôn đảm bảo logic rõ ràng và có comment đầy đủ.
3. Đặt biệt chú ý đến bảo mật khi triển khai các tính năng xác thực và phân quyền.
4. Khi làm việc với tệp, hãy đảm bảo xử lý đúng cách để tránh lỗ hổng bảo mật.
5. Luôn kiểm tra dữ liệu đầu vào từ người dùng để tránh các cuộc tấn công như SQL injection. 