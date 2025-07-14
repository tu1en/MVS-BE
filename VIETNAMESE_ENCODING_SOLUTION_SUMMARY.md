# 🇻🇳 TÓM TẮT GIẢI PHÁP ENCODING TIẾNG VIỆT

## 🔍 **PHÂN TÍCH NGUYÊN NHÂN GỐC RỄ**

### **1. Vấn đề Constraints Dependencies**
```
Msg 5074: The object 'UK_6dotkott2kjsp8vw4d0m25fb7' is dependent on column 'email'
Msg 4922: ALTER TABLE ALTER COLUMN email failed because one or more objects access this column
```

**Nguyên nhân:** SQL Server không cho phép thay đổi cột khi có constraints (UNIQUE, CHECK, FOREIGN KEY) phụ thuộc vào cột đó.

### **2. Vấn đề Database Collation**
- Database sử dụng `SQL_Latin1_General_CP1_CI_AS` (không hỗ trợ Unicode tốt)
- Cần sử dụng `Vietnamese_CI_AS` để hỗ trợ đầy đủ ký tự tiếng Việt

### **3. Vấn đề Column Definitions**
- Một số cột sử dụng `VARCHAR` thay vì `NVARCHAR`
- Một số cột sử dụng `TEXT` thay vì `NTEXT`
- Thiếu collation specification cho các cột text

### **4. Vấn đề Dữ liệu Corrupt**
- Dữ liệu như "Toán cao c?p A1", "VÄƒn há»?c Viá»╪t Nam" đã bị lưu sai
- Cần clean up và fix dữ liệu hiện có

## 🛠️ **GIẢI PHÁP HOÀN CHỈNH**

### **File đã tạo:**
1. ✅ `comprehensive-vietnamese-encoding-fix-v2.sql` - Script SQL chính
2. ✅ `run-vietnamese-encoding-fix-v2.bat` - Script batch tự động
3. ✅ `VIETNAMESE_ENCODING_FIX_INSTRUCTIONS.md` - Hướng dẫn chi tiết
4. ✅ `application.properties` - Cấu hình Spring Boot đã cập nhật
5. ✅ `DatabaseConfig.java` - Cấu hình database đã cập nhật
6. ✅ Entity models - Đã cập nhật column definitions

### **Các bước thực hiện:**

#### **Bước 1: Chạy Script V2 (Tự động)**
```bash
# Cách 1: Chạy script batch (Đơn giản)
run-vietnamese-encoding-fix-v2.bat

# Cách 2: Chạy thủ công
sqlcmd -S localhost -U sa -P [password] -d SchoolManagementDB -i comprehensive-vietnamese-encoding-fix-v2.sql
```

#### **Bước 2: Restart Application**
```bash
cd backend/doproject
mvn clean compile
mvn spring-boot:run
```

#### **Bước 3: Kiểm tra kết quả**
- Frontend hiển thị tiếng Việt đúng
- Tạo dữ liệu mới để test
- Kiểm tra database collation

## 📋 **SCRIPT V2 THỰC HIỆN GÌ?**

### **1. Xử lý Constraints (Điểm mới)**
```sql
-- Tìm và lưu constraints
CREATE TABLE #ConstraintsToRestore (...)
INSERT INTO #ConstraintsToRestore SELECT ...

-- Xóa constraints tạm thời
ALTER TABLE Users DROP CONSTRAINT UK_6dotkott2kjsp8vw4d0m25fb7;

-- Thay đổi cột
ALTER TABLE Users ALTER COLUMN email NVARCHAR(255) COLLATE Vietnamese_CI_AS NOT NULL;

-- Tạo lại constraints
ALTER TABLE Users ADD CONSTRAINT UK_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);
```

### **2. Thay đổi Column Definitions**
```sql
-- Từ VARCHAR → NVARCHAR
ALTER TABLE Users ALTER COLUMN email NVARCHAR(255) COLLATE Vietnamese_CI_AS NOT NULL;

-- Từ TEXT → NTEXT  
ALTER TABLE Assignments ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS;
```

### **3. Fix Dữ liệu Corrupt**
```sql
-- Sửa tên bị lỗi encoding
UPDATE Users SET full_name = N'Phạm Văn Nam' WHERE full_name LIKE '%Ph?m Van Nam%';
UPDATE Classrooms SET name = N'Toán cao cấp A1' WHERE name LIKE '%Toán cao c?p A1%';
```

### **4. Kiểm tra và Validation**
```sql
-- Kiểm tra collation
SELECT t.name, c.name, c.collation_name FROM sys.tables t JOIN sys.columns c...

-- Kiểm tra dữ liệu
SELECT TOP 5 full_name, email FROM Users WHERE full_name NOT LIKE '%?%';
```

## 🎯 **KẾT QUẢ MONG ĐỢI**

### **Trước khi fix:**
- ❌ "Toán cao c?p A1"
- ❌ "Ph?m Van Nam"
- ❌ "VÄƒn há»?c Viá»╪t Nam"
- ❌ "BA i lA m cá»§a em"

### **Sau khi fix:**
- ✅ "Toán cao cấp A1"
- ✅ "Phạm Văn Nam"
- ✅ "Văn học Việt Nam"
- ✅ "Bài làm của em"

## 🔧 **TROUBLESHOOTING**

### **Lỗi 1: Script không chạy được**
```bash
# Kiểm tra sqlcmd
where sqlcmd

# Kiểm tra file tồn tại
dir comprehensive-vietnamese-encoding-fix-v2.sql

# Kiểm tra kết nối
sqlcmd -S localhost -U sa -P [password] -Q "SELECT 1"
```

### **Lỗi 2: Constraints vẫn còn**
```sql
-- Xem constraints còn lại
SELECT * FROM sys.key_constraints WHERE parent_object_id = OBJECT_ID('Users');

-- Xóa thủ công
ALTER TABLE Users DROP CONSTRAINT [constraint_name];
```

### **Lỗi 3: Dữ liệu vẫn lỗi**
```sql
-- Kiểm tra dữ liệu lỗi
SELECT * FROM Users WHERE full_name LIKE '%?%' OR full_name LIKE '%A¡%';

-- Sửa thủ công
UPDATE Users SET full_name = N'Tên đúng' WHERE id = [id];
```

## 📊 **KIỂM TRA SAU KHI HOÀN THÀNH**

### **1. Database Level**
```sql
-- Kiểm tra collation
SELECT DATABASEPROPERTYEX('SchoolManagementDB', 'Collation');

-- Kiểm tra column definitions
SELECT t.name, c.name, c.collation_name, ty.name 
FROM sys.tables t JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.types ty ON c.user_type_id = ty.user_type_id
WHERE c.name IN ('email', 'full_name', 'title', 'description');
```

### **2. Application Level**
```java
// Kiểm tra application.properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=SchoolManagementDB;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=true;characterEncoding=UTF-8;useUnicode=true;collation=Vietnamese_CI_AS

// Kiểm tra entity annotations
@Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
private String fullName;
```

### **3. Frontend Level**
- Tạo user mới với tên tiếng Việt
- Tạo classroom với tên tiếng Việt
- Tạo assignment với nội dung tiếng Việt
- Kiểm tra hiển thị trên UI

## 🎉 **HOÀN THÀNH**

Sau khi thực hiện script V2, bạn sẽ có hệ thống hoàn toàn hỗ trợ tiếng Việt:

- ✅ **Database collation**: Vietnamese_CI_AS
- ✅ **Column definitions**: NVARCHAR/NTEXT 
- ✅ **Application config**: Unicode enabled
- ✅ **Entity models**: Proper annotations
- ✅ **Data cleanup**: Corrupt data fixed
- ✅ **Constraints**: Properly restored

## 📞 **HỖ TRỢ**

Nếu gặp vấn đề:
1. Đọc `VIETNAMESE_ENCODING_FIX_INSTRUCTIONS.md`
2. Chạy script kiểm tra trong hướng dẫn
3. Cung cấp log chi tiết nếu cần hỗ trợ

---

**Lưu ý**: Script V2 đã xử lý vấn đề constraints dependencies mà bạn gặp phải. Bây giờ bạn có thể chạy script một cách an toàn! 