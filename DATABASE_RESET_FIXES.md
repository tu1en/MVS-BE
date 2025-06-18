# Database Reset Fixes Applied

## Issues Fixed

### 1. Database Not Resetting on Startup
**Problem**: Mỗi khi chạy Spring Boot, database không được reset hoàn toàn, dữ liệu cũ vẫn còn tồn tại.

**Root Cause**: 
- DataLoader chỉ chạy khi `userRepository.count() == 0`
- Cấu hình Hibernate không force reset hoàn toàn
- Dữ liệu cũ không được xóa trước khi load dữ liệu mới

**Solution**: 

#### 1.1. Sửa DataLoader.java
```java
@Override
public void run(String... args) throws Exception {
    // Always clear existing data and reload fresh data
    clearAllData();
    
    // Create sample users
    List<User> users = CreateUsers();
    
    // Create sample blogs
    CreateSampleBlogs(users);
    
    // Create sample accomplishments
    CreateAccomplishments();
    
    // Create sample requests
    CreateRequests();
    
    System.out.println("✅ DataLoader: All data has been reset and reloaded successfully!");
}

private void clearAllData() {
    System.out.println("🗑️ DataLoader: Clearing all existing data...");
    
    // Clear data in reverse order of dependencies to avoid foreign key constraints
    accomplishmentRepository.deleteAll();
    requestRepository.deleteAll();
    blogRepository.deleteAll();
    userRepository.deleteAll();
    
    System.out.println("✅ DataLoader: All existing data cleared successfully!");
}
```

#### 1.2. Cập nhật application.properties
```properties
# Force complete database reset on each startup
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
```

### 2. Files Modified

1. **`DataLoader.java`** - Sửa logic để luôn xóa và load lại dữ liệu
2. **`application.properties`** - Cập nhật cấu hình Hibernate để force reset
3. **`reset-database.bat`** - Tạo script để dễ dàng reset database

### 3. How It Works Now

1. **Khi ứng dụng khởi động**:
   - Hibernate sẽ xóa tất cả bảng cũ (`create-drop`)
   - Tạo lại tất cả bảng mới
   - DataLoader sẽ xóa tất cả dữ liệu cũ (nếu có)
   - Load lại dữ liệu mẫu từ DataLoader

2. **Thứ tự xóa dữ liệu**:
   - `accomplishmentRepository.deleteAll()` (có foreign key đến User)
   - `requestRepository.deleteAll()` (có foreign key đến User)
   - `blogRepository.deleteAll()` (có foreign key đến User)
   - `userRepository.deleteAll()` (bảng chính)

3. **Thứ tự tạo dữ liệu**:
   - Tạo Users trước
   - Tạo Blogs (cần User làm author)
   - Tạo Accomplishments (cần User)
   - Tạo Requests (cần User)

### 4. Usage

#### 4.1. Tự động reset mỗi lần chạy
```bash
mvn spring-boot:run
```

#### 4.2. Sử dụng script reset
```bash
reset-database.bat
```

### 5. Sample Data Loaded

Mỗi lần reset sẽ tạo:
- **Users**: admin, manager, teacher, student
- **Blogs**: 4 bài blog mẫu với nội dung đa dạng
- **Accomplishments**: Thành tích mẫu cho student
- **Requests**: Yêu cầu đăng ký mẫu

### 6. Console Output

Khi chạy sẽ thấy:
```
🗑️ DataLoader: Clearing all existing data...
✅ DataLoader: All existing data cleared successfully!
✅ DataLoader: All data has been reset and reloaded successfully!
```

## Notes

- ⚠️ **Cảnh báo**: Tất cả dữ liệu sẽ bị xóa mỗi lần chạy ứng dụng
- 🔄 **Reset hoàn toàn**: Không có dữ liệu nào được giữ lại
- 📊 **Dữ liệu mẫu**: Chỉ có dữ liệu từ DataLoader được load
- 🚀 **Performance**: Quá trình reset nhanh và hiệu quả 