# Dữ liệu mẫu cho Classroom "Tôn"

## Tổng quan
Đã tạo thành công dữ liệu mẫu cho classroom "Toán cao cấp A1" (ID = 1) theo yêu cầu, bao gồm:

## 📊 Dữ liệu đã tạo

### 1. 👥 5 Học sinh mới
- **Nguyễn Văn An** - `nguyenvanan.ton@student.edu.vn`
- **Trần Thị Bình** - `tranthibinh.ton@student.edu.vn`
- **Lê Hoàng Cường** - `lehoangcuong.ton@student.edu.vn`
- **Phạm Thị Dung** - `phamthidung.ton@student.edu.vn`
- **Hoàng Văn Em** - `hoangvanem.ton@student.edu.vn`

**Thông tin đăng nhập:**
- Username: `student_ton_1` đến `student_ton_5`
- Password: `123456` (đã được mã hóa)
- Role: STUDENT (role_id = 1)
- Tất cả đã được đăng ký vào classroom "Tôn"

### 2. 📝 5 Bài tập cần chấm điểm
Các bài tập đã có submissions từ học sinh nhưng **chưa được chấm điểm**:

1. **Bài tập Đạo hàm và Tích phân** - 4/5 học sinh đã nộp
2. **Thực hành Giải phương trình vi phân** - 3/5 học sinh đã nộp
3. **Bài tập Ma trận và Định thức nâng cao** - 5/5 học sinh đã nộp
4. **Ứng dụng Toán học trong Kinh tế** - 2/5 học sinh đã nộp
5. **Bài tập tổng hợp Giải tích** - 3/5 học sinh đã nộp

**Tổng cộng: 17 submissions cần chấm điểm**

### 3. ⏰ 5 Bài tập sắp hết hạn
Due date trong 1-5 ngày tới, **chưa có submissions**:

1. **Bài kiểm tra Giới hạn và Liên tục** - hết hạn sau 1 ngày
2. **Thực hành Tính tích phân bằng phương pháp thế** - hết hạn sau 2 ngày
3. **Bài tập Chuỗi số và Chuỗi hàm** - hết hạn sau 3 ngày
4. **Ứng dụng Đạo hàm trong Hình học** - hết hạn sau 4 ngày
5. **Bài tập Phương trình tham số** - hết hạn sau 5 ngày

### 4. 📅 5 Bài tập đã hết hạn
Due date đã qua 1-5 ngày, có một số submissions (một số đã chấm, một số chưa):

1. **Bài tập Hàm số một biến** - 3 submissions (2 đã chấm)
2. **Thực hành Tính đạo hàm cấp cao** - 4 submissions (1 đã chấm)
3. **Bài kiểm tra Tích phân bội** - 2 submissions (chưa chấm)
4. **Ứng dụng Toán học trong Vật lý** - 0 submissions
5. **Bài tập tổng hợp Đại số tuyến tính** - 0 submissions

**Tổng cộng: 9 submissions (3 đã chấm, 6 chưa chấm)**

## 📈 Thống kê tổng quan

- **Tổng số assignments:** 15 (5 cần chấm + 5 sắp hết hạn + 5 đã hết hạn)
- **Tổng số submissions:** 26
  - Chưa chấm điểm: 23 submissions
  - Đã chấm điểm: 3 submissions
  - Điểm trung bình: 89.0/100

## 🛠️ Cách sử dụng

### Scripts đã tạo:
1. **`ton-classroom-sample-data.sql`** - Tạo học sinh và assignments
2. **`ton-classroom-submissions.sql`** - Tạo submissions
3. **`verify-ton-classroom-data.sql`** - Kiểm tra và xác minh dữ liệu

### Chạy scripts:
```bash
# 1. Tạo dữ liệu cơ bản
sqlcmd -S localhost -U sa -P 12345678 -i ton-classroom-sample-data.sql

# 2. Tạo submissions
sqlcmd -S localhost -U sa -P 12345678 -i ton-classroom-submissions.sql

# 3. Kiểm tra dữ liệu
sqlcmd -S localhost -U sa -P 12345678 -i verify-ton-classroom-data.sql
```

## 🎯 Mục đích sử dụng

Dữ liệu này phù hợp để test các tính năng:

### 1. Chấm điểm bài tập
- 17 submissions cần chấm điểm
- Có thể test bulk grading, individual grading
- Test feedback và scoring system

### 2. Quản lý deadline
- 5 bài tập sắp hết hạn để test notification system
- 5 bài tập đã hết hạn để test late submission handling

### 3. Dashboard và báo cáo
- Thống kê submissions theo học sinh
- Thống kê assignments theo trạng thái
- Progress tracking

### 4. Notification system
- Thông báo bài tập sắp hết hạn
- Thông báo có bài cần chấm
- Thông báo điểm số mới

## ✅ Đảm bảo chất lượng

- ✅ Sử dụng dữ liệu thực tế, không có mock data
- ✅ Tính nhất quán của dữ liệu được đảm bảo
- ✅ Tuân thủ business rules
- ✅ Foreign key relationships chính xác
- ✅ Đã kiểm tra và xác minh sau khi tạo

## 🔧 Maintenance

Để xóa dữ liệu test (nếu cần):
```sql
-- Xóa submissions
DELETE FROM submissions WHERE assignment_id IN (
    SELECT id FROM assignments WHERE classroom_id = 1 AND title LIKE '%Tôn%'
);

-- Xóa assignments
DELETE FROM assignments WHERE classroom_id = 1 AND (
    title LIKE '%Cần chấm điểm%' OR 
    title LIKE '%Sắp hết hạn%' OR 
    title LIKE '%Đã hết hạn%'
);

-- Xóa enrollments
DELETE FROM classroom_enrollments WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%ton@student.edu.vn'
);

-- Xóa users
DELETE FROM users WHERE email LIKE '%ton@student.edu.vn';
```

---
**Tạo bởi:** Data Seeder Script  
**Ngày tạo:** 2025-07-12  
**Database:** SchoolManagementDB  
**Classroom:** Toán cao cấp A1 (ID = 1)
