@echo off
chcp 65001 > nul
echo.
echo 🇻🇳 ===============================================
echo    VIETNAMESE ENCODING FIX SCRIPT V2
echo    Sửa lỗi encoding tiếng Việt cho SQL Server
echo ===============================================
echo.

echo 🔍 Kiểm tra môi trường...
echo.

:: Kiểm tra sqlcmd có tồn tại không
where sqlcmd >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Không tìm thấy sqlcmd. Vui lòng cài đặt SQL Server Command Line Tools.
    echo 📥 Tải tại: https://docs.microsoft.com/en-us/sql/tools/sqlcmd-utility
    pause
    exit /b 1
)

:: Kiểm tra file script có tồn tại không
if not exist "comprehensive-vietnamese-encoding-fix-v2.sql" (
    echo ❌ Không tìm thấy file comprehensive-vietnamese-encoding-fix-v2.sql
    echo 📍 Hãy chắc chắn file này có trong thư mục hiện tại
    pause
    exit /b 1
)

echo ✅ Tìm thấy sqlcmd
echo ✅ Tìm thấy script SQL
echo.

:: Cấu hình database
set SERVER=localhost
set DATABASE=SchoolManagementDB
set USERNAME=sa
set /p PASSWORD=🔑 Nhập password cho SQL Server (sa): 

echo.
echo 🚀 Bắt đầu thực hiện script...
echo.
echo ⏳ Đang kết nối đến SQL Server...
echo    Server: %SERVER%
echo    Database: %DATABASE%
echo    Username: %USERNAME%
echo.

:: Kiểm tra kết nối trước
sqlcmd -S %SERVER% -U %USERNAME% -P %PASSWORD% -d %DATABASE% -Q "SELECT 1 AS ConnectionTest" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Không thể kết nối đến SQL Server
    echo 🔧 Kiểm tra lại:
    echo    - SQL Server đang chạy
    echo    - Thông tin kết nối đúng
    echo    - Firewall không chặn
    pause
    exit /b 1
)

echo ✅ Kết nối thành công
echo.

:: Tạo backup trước khi thay đổi
echo 💾 Tạo backup database...
set BACKUP_PATH=C:\Temp\SchoolManagementDB_BeforeEncodingFix_v2_%date:~-4,4%%date:~-10,2%%date:~-7,2%.bak
mkdir C:\Temp 2>nul
sqlcmd -S %SERVER% -U %USERNAME% -P %PASSWORD% -Q "BACKUP DATABASE %DATABASE% TO DISK = '%BACKUP_PATH%'" >nul 2>&1
if %errorlevel% eq 0 (
    echo ✅ Backup thành công: %BACKUP_PATH%
) else (
    echo ⚠️ Không thể tạo backup (có thể do quyền truy cập)
    echo 🤔 Bạn có muốn tiếp tục không? (Y/N)
    set /p choice=
    if /i "!choice!" neq "Y" (
        echo ❌ Hủy bỏ thực hiện script
        pause
        exit /b 1
    )
)
echo.

:: Thực hiện script chính
echo 🛠️ Thực hiện script Vietnamese Encoding Fix V2...
echo.
echo 📋 Log chi tiết:
echo ================================================
sqlcmd -S %SERVER% -U %USERNAME% -P %PASSWORD% -d %DATABASE% -i comprehensive-vietnamese-encoding-fix-v2.sql
echo ================================================
echo.

if %errorlevel% eq 0 (
    echo 🎉 Script hoàn thành thành công!
    echo.
    echo 📊 Kiểm tra kết quả...
    echo.
    
    :: Kiểm tra collation
    echo 🔍 Kiểm tra collation của các cột:
    sqlcmd -S %SERVER% -U %USERNAME% -P %PASSWORD% -d %DATABASE% -Q "SELECT t.name AS TableName, c.name AS ColumnName, c.collation_name AS Collation, ty.name AS DataType FROM sys.tables t JOIN sys.columns c ON t.object_id = c.object_id JOIN sys.types ty ON c.user_type_id = ty.user_type_id WHERE c.name IN ('email', 'full_name', 'title', 'description', 'content') AND t.name IN ('Users', 'Assignments', 'Submissions', 'Classrooms') ORDER BY t.name, c.name"
    
    echo.
    echo 📋 Kiểm tra dữ liệu đã sửa:
    sqlcmd -S %SERVER% -U %USERNAME% -P %PASSWORD% -d %DATABASE% -Q "SELECT TOP 3 full_name, email FROM Users WHERE full_name NOT LIKE '%%?%%' AND full_name NOT LIKE '%%A¡%%'"
    
    echo.
    echo ✅ HOÀN THÀNH!
    echo.
    echo 📝 Các bước tiếp theo:
    echo    1. Restart ứng dụng Spring Boot
    echo    2. Kiểm tra frontend hiển thị tiếng Việt
    echo    3. Tạo dữ liệu mới để test
    echo.
    echo 🎯 Hướng dẫn chi tiết: VIETNAMESE_ENCODING_FIX_INSTRUCTIONS.md
    
) else (
    echo ❌ Script gặp lỗi!
    echo.
    echo 🔧 Các bước xử lý lỗi:
    echo    1. Kiểm tra log ở trên
    echo    2. Đọc hướng dẫn: VIETNAMESE_ENCODING_FIX_INSTRUCTIONS.md
    echo    3. Khôi phục backup nếu cần: %BACKUP_PATH%
    echo.
    echo 📞 Liên hệ hỗ trợ nếu không tự giải quyết được
)

echo.
echo 🔚 Nhấn phím bất kỳ để thoát...
pause >nul 