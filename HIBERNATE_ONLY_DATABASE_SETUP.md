# Hibernate-Only Database Setup

## Tổng quan
Dự án đã được chuyển đổi để sử dụng Hibernate hoàn toàn cho việc quản lý database, loại bỏ việc sử dụng các file SQL và chỉ sử dụng DataLoader.java để insert dữ liệu mẫu.

## Những thay đổi đã thực hiện

### 1. Cập nhật application.properties
- **Thay đổi**: Cấu hình để sử dụng Hibernate hoàn toàn
- **Chi tiết**:
  - `spring.sql.init.mode=never` - Tắt việc khởi tạo từ SQL scripts
  - `spring.jpa.defer-datasource-initialization=false` - Không defer datasource initialization
  - `spring.jpa.hibernate.ddl-auto=create-drop` - Tạo và xóa database schema mỗi lần khởi động

### 2. Xóa các file SQL không cần thiết
Các file sau đã được xóa:
- `schema.sql`
- `schema-extensions.sql`
- `schema-sqlserver.sql`
- `data.sql`
- `data-sqlserver.sql`
- `data-dynamic.sql`
- `student-data.sql`
- `homework-test-data.sql`
- `migration-student-communication-sqlserver.sql`
- `drop-tables.sql`
- `db/migration/V21__add_timetable_events_table.sql`

### 3. Cập nhật DataLoader.java
- **Thay đổi**: Đơn giản hóa DataLoader để chỉ tạo dữ liệu mẫu cho các entity cơ bản
- **Entity được hỗ trợ**:
  - User (Admin, Manager, Teacher, Student)
  - Blog (4 bài blog mẫu với đầy đủ thông tin)
  - Accomplishment (3 thành tích mẫu cho student)
  - Request (2 yêu cầu đăng ký role mẫu)

### 4. Logic được giữ nguyên
- Tất cả logic nghiệp vụ vẫn được giữ nguyên
- Các entity và relationship vẫn hoạt động bình thường
- Chỉ thay đổi cách thức khởi tạo database và dữ liệu mẫu

## Lợi ích của việc sử dụng Hibernate hoàn toàn

### 1. Tính linh hoạt
- Hibernate tự động tạo schema dựa trên entity annotations
- Không cần maintain các file SQL riêng biệt
- Dễ dàng thay đổi database schema bằng cách cập nhật entity

### 2. Tính nhất quán
- Schema luôn đồng bộ với entity definitions
- Không có xung đột giữa SQL scripts và entity mappings
- Đảm bảo tính toàn vẹn dữ liệu

### 3. Dễ bảo trì
- Chỉ cần maintain entity classes
- Không cần quản lý nhiều file SQL
- Dễ dàng thêm/sửa/xóa fields

### 4. Cross-platform
- Hibernate hỗ trợ nhiều loại database khác nhau
- Không cần viết SQL riêng cho từng database
- Dễ dàng chuyển đổi giữa các database

## Cách sử dụng

### 1. Khởi động ứng dụng
```bash
# Chạy backend
./start-backend-fixed.bat
```

### 2. Database sẽ được tạo tự động
- Hibernate sẽ tạo tất cả tables dựa trên entity annotations
- DataLoader sẽ insert dữ liệu mẫu
- Database sẽ được reset mỗi lần khởi động (create-drop mode)

### 3. Thêm entity mới
1. Tạo entity class với annotations
2. Tạo repository interface
3. Thêm dữ liệu mẫu vào DataLoader (nếu cần)
4. Hibernate sẽ tự động tạo table

## Lưu ý quan trọng

### 1. Production Environment
- Thay đổi `spring.jpa.hibernate.ddl-auto` thành `validate` hoặc `update` cho production
- Không sử dụng `create-drop` trong production vì sẽ xóa dữ liệu

### 2. Migration
- Nếu cần migration dữ liệu, sử dụng Flyway hoặc Liquibase
- Hoặc viết migration scripts riêng

### 3. Performance
- Hibernate có thể chậm hơn raw SQL cho một số operation phức tạp
- Sử dụng @Query annotation cho custom queries khi cần

## Kết luận
Việc chuyển đổi sang sử dụng Hibernate hoàn toàn đã thành công, đảm bảo tính linh hoạt và dễ bảo trì của hệ thống. Tất cả logic nghiệp vụ vẫn được giữ nguyên, chỉ thay đổi cách thức quản lý database schema và dữ liệu mẫu. 