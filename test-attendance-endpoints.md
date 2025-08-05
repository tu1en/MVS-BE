# Test Staff Attendance Data

## 🚀 How to Test the New Staff Attendance Data

After running your application, the `AttendanceDataLoader` will automatically create test data for managers and accountants.

## 📊 Test Endpoints

### 1. **View All Staff Users** (No authentication needed for testing)
```bash
GET http://localhost:8080/api/test/attendance/staff-users
```
This shows all managers, accountants, and teachers in the system.

### 2. **View All Attendance Logs**
```bash
GET http://localhost:8080/api/test/attendance/all-staff-logs
```
Shows all attendance logs with statistics by role.

### 3. **View Logs by Role**
```bash
# Manager attendance logs
GET http://localhost:8080/api/test/attendance/by-role/Manager

# Accountant attendance logs  
GET http://localhost:8080/api/test/attendance/by-role/Accountant

# Teacher attendance logs
GET http://localhost:8080/api/test/attendance/by-role/Teacher
```

### 4. **View Logs by Date**
```bash
GET http://localhost:8080/api/test/attendance/by-date?date=2024-01-15
```

## 🔐 Main Attendance Endpoints (Require Authentication)

### **For Managers and Accountants** (Role: MANAGER or ACCOUNTANT):

```bash
# Get all staff attendance for today
GET http://localhost:8080/api/attendance/all-logs?date=2024-01-15
Authorization: Bearer <your-manager-token>

# Get teacher attendance by shift
GET http://localhost:8080/api/attendance/teacher-status?date=2024-01-15&shift=MORNING
Authorization: Bearer <your-manager-token>

# Get attendance by shift
GET http://localhost:8080/api/attendance/daily-shift?date=2024-01-15&shift=MORNING
Authorization: Bearer <your-manager-token>
```

### **For Any Staff Member** (Check own attendance):

```bash
# Get your own attendance summary
GET http://localhost:8080/api/attendance/my-attendance-summary?userId=<your-user-id>
Authorization: Bearer <your-token>

# Get attendance history for date range
GET http://localhost:8080/api/attendance/my-history-range?userId=<your-user-id>&startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer <your-token>
```

## 📝 Test Data Details

The system creates:
- **30 days** of attendance data (excluding weekends)
- **1-2 shifts per day** for each staff member
- **Shifts**: MORNING, AFTERNOON, EVENING
- **Status distribution**:
  - 85% PRESENT
  - 10% LATE  
  - 5% ABSENT
- **Realistic check-in/out times** based on shift and status

## 🔍 Sample Response Format

```json
{
  "id": 123,
  "userId": 45,
  "userName": "John Manager",
  "role": "Manager", 
  "department": "Phòng Manager",
  "date": "2024-01-15",
  "shift": "MORNING",
  "checkIn": "08:05:00",
  "checkOut": "12:10:00", 
  "status": "PRESENT"
}
```

## 🧪 Testing Steps

1. **Start your application** - Data will be loaded automatically
2. **Check test endpoints** to verify data exists
3. **Login as Manager/Accountant** to get authentication token
4. **Test authenticated endpoints** with the token
5. **Verify different date ranges and filters work**

## 🗑️ Clear Test Data (if needed)
```bash
DELETE http://localhost:8080/api/test/attendance/clear-logs
```

---
**Note**: The test controller is for development only. Remove it in production!