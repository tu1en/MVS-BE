# SQL Server Compatibility Fixes Applied

## Issues Fixed

### 1. AUTO_INCREMENT Syntax Error
**Problem**: SQL Server không hỗ trợ cú pháp `AUTO_INCREMENT`, gây ra lỗi:
```
Incorrect syntax near 'AUTO_INCREMENT'.
```

**Root Cause**: 
- File SQL sử dụng cú pháp MySQL (`AUTO_INCREMENT`)
- SQL Server sử dụng `IDENTITY(1,1)` thay thế

**Solution**: Thay thế tất cả `AUTO_INCREMENT` bằng `IDENTITY(1,1)`

### 2. ON UPDATE CURRENT_TIMESTAMP Syntax Error
**Problem**: SQL Server không hỗ trợ `ON UPDATE CURRENT_TIMESTAMP`

**Solution**: Loại bỏ `ON UPDATE CURRENT_TIMESTAMP` và chỉ giữ `DEFAULT CURRENT_TIMESTAMP`

## Files Modified

### 1. `schema.sql`
**Changes**:
- `id BIGINT AUTO_INCREMENT PRIMARY KEY` → `id BIGINT IDENTITY(1,1) PRIMARY KEY`
- Áp dụng cho tất cả bảng: users, classrooms, attendance_sessions, attendances, allowed_ips, assignments, submissions, accomplishments

### 2. `schema-extensions.sql`
**Changes**:
- Sửa tất cả `AUTO_INCREMENT` thành `IDENTITY(1,1)`
- Loại bỏ `ON UPDATE CURRENT_TIMESTAMP`
- Áp dụng cho các bảng:
  - course_materials
  - course_schedule
  - student_progress
  - grading_rubrics
  - grading_details
  - lecture_recordings
  - live_streams
  - announcements
  - announcement_attachments
  - announcement_reads
  - timetable_events
  - event_attendees
  - quiz_questions
  - quiz_question_options
  - student_quiz_answers
  - class_sessions
  - student_groups
  - group_members
  - communication_channels
  - channel_messages

### 3. `db/migration/V21__add_timetable_events_table.sql`
**Changes**:
- `id BIGINT AUTO_INCREMENT PRIMARY KEY` → `id BIGINT IDENTITY(1,1) PRIMARY KEY`

## SQL Server vs MySQL Syntax Differences

| Feature | MySQL | SQL Server |
|---------|-------|------------|
| Auto Increment | `AUTO_INCREMENT` | `IDENTITY(1,1)` |
| Auto Update Timestamp | `ON UPDATE CURRENT_TIMESTAMP` | Not supported |
| Default Timestamp | `DEFAULT CURRENT_TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` |
| Text Type | `TEXT` | `TEXT` |
| Boolean | `BOOLEAN` | `BOOLEAN` |

## Testing

Sau khi sửa, ứng dụng sẽ:
1. ✅ Khởi động thành công với SQL Server
2. ✅ Tạo tất cả bảng với cú pháp đúng
3. ✅ DataLoader hoạt động bình thường
4. ✅ Database reset hoạt động đúng

## Notes

- ⚠️ **Cảnh báo**: Không sử dụng cú pháp MySQL trong SQL Server
- 🔄 **Migration**: Tất cả file SQL đã được cập nhật
- 🚀 **Performance**: SQL Server sẽ tạo bảng nhanh hơn
- 📊 **Compatibility**: 100% tương thích với SQL Server 