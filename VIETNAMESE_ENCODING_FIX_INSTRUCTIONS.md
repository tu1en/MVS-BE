# 🛠️ HƯỚNG DẪN SỬA LỖI ENCODING TIẾNG VIỆT

## 🚨 **VẤN ĐỀ BẠN VỪA GẶP**

Lỗi bạn vừa gặp là:
```
Msg 5074, Level 16, State 1, Line 24
The object 'UK_6dotkott2kjsp8vw4d0m25fb7' is dependent on column 'email'.
Msg 4922, Level 16, State 9, Line 24
ALTER TABLE ALTER COLUMN email failed because one or more objects access this column.
```

**Nguyên nhân:** SQL Server không cho phép thay đổi cột khi còn có **constraints** (UNIQUE, CHECK, FOREIGN KEY) phụ thuộc vào cột đó.

## 🆕 **GIẢI PHÁP MỚI - SCRIPT V2**

Tôi đã tạo script mới `comprehensive-vietnamese-encoding-fix-v2.sql` xử lý vấn đề constraints:

### **Các bước script V2 thực hiện:**

1. **🔍 Tìm và lưu tất cả constraints** phụ thuộc vào các cột cần sửa
2. **🗑️ Xóa tạm thời constraints** để có thể thay đổi cột
3. **🔧 Thay đổi cấu trúc cột** để hỗ trợ Unicode
4. **🔄 Tạo lại constraints** sau khi đã sửa xong
5. **📝 Sửa dữ liệu bị corrupt** 
6. **✅ Kiểm tra kết quả**

## 📋 **CÁCH SỬ DỤNG**

### **Bước 1: Chạy Script V2**
```bash
# Trong SQL Server Management Studio hoặc sqlcmd
sqlcmd -S localhost -U sa -P 12345678 -d SchoolManagementDB -i comprehensive-vietnamese-encoding-fix-v2.sql
```

### **Bước 2: Kiểm tra Log**
Script sẽ hiển thị từng bước chi tiết:
- ✅ Thành công
- ⚠️ Cảnh báo
- ❌ Lỗi (nếu có)

### **Bước 3: Kiểm tra Kết quả**
```sql
-- Xem collation của các cột
SELECT 
    t.name AS TableName,
    c.name AS ColumnName,
    c.collation_name AS Collation,
    ty.name AS DataType
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.types ty ON c.user_type_id = ty.user_type_id
WHERE c.name IN ('email', 'full_name', 'title', 'description', 'content')
AND t.name IN ('Users', 'Assignments', 'Submissions', 'Classrooms')
ORDER BY t.name, c.name
```

## 🔧 **NẾU VẪN GẶP LỖI**

### **Lỗi 1: Không tìm thấy constraint**
```sql
-- Kiểm tra constraints thủ công
SELECT 
    t.name AS TableName,
    c.name AS ColumnName,
    kc.name AS ConstraintName,
    kc.type_desc AS ConstraintType
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.key_constraints kc ON t.object_id = kc.parent_object_id
WHERE c.name = 'email'
```

### **Lỗi 2: Không thể xóa constraint**
```sql
-- Xóa constraint thủ công
ALTER TABLE Users DROP CONSTRAINT UK_6dotkott2kjsp8vw4d0m25fb7;
```

### **Lỗi 3: Dữ liệu không hợp lệ**
```sql
-- Kiểm tra dữ liệu có ký tự lạ
SELECT TOP 10 email, full_name 
FROM Users 
WHERE email LIKE '%?%' OR full_name LIKE '%?%'
```

## 📝 **SAU KHI CHẠY SCRIPT**

### **1. Restart Application**
```bash
# Trong thư mục backend
cd backend/doproject
mvn clean compile
mvn spring-boot:run
```

### **2. Kiểm tra Frontend**
- Mở trình duyệt đến `http://localhost:3000`
- Kiểm tra xem tiếng Việt hiển thị đúng không
- Tạo dữ liệu mới có tiếng Việt để test

### **3. Kiểm tra Database**
```sql
-- Kiểm tra dữ liệu mới
SELECT TOP 5 * FROM Users WHERE created_at > GETDATE() - 1
SELECT TOP 5 * FROM Classrooms WHERE created_at > GETDATE() - 1
SELECT TOP 5 * FROM Assignments WHERE created_at > GETDATE() - 1
```

## 🏁 **HOÀN THÀNH**

Sau khi thực hiện script V2 thành công, bạn sẽ có:

- ✅ **Database collation** hỗ trợ tiếng Việt
- ✅ **Cấu trúc cột** sử dụng NVARCHAR/NTEXT
- ✅ **Dữ liệu corrupt** đã được sửa
- ✅ **Constraints** được tạo lại đúng cách
- ✅ **Application config** đã được cập nhật

## 📞 **HỖ TRỢ**

Nếu gặp vấn đề:
1. Gửi **toàn bộ log** từ script
2. Chạy **kiểm tra collation** ở trên
3. Chụp **screenshot lỗi** nếu có

## 📚 **TÀI LIỆU THAM KHẢO**

- [SQL Server Collations](https://docs.microsoft.com/en-us/sql/relational-databases/collations/collation-and-unicode-support)
- [Spring Boot Database Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.datasource)
- [Vietnamese Encoding Best Practices](https://docs.microsoft.com/en-us/sql/relational-databases/collations/collation-and-unicode-support#Vietnamese_CI_AS) 