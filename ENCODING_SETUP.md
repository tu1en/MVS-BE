# UTF-8 Encoding Setup Guide

## 🎯 Mục đích
Hướng dẫn này giúp fix vấn đề encoding/character display khi chạy ứng dụng Spring Boot, đảm bảo hiển thị đúng tiếng Việt và Unicode.

## 🔧 Các cấu hình đã được triển khai

### 1. **Logback Configuration** (`src/main/resources/logback-spring.xml`)
```xml
<encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
    <charset>UTF-8</charset>
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} -- %msg%n</pattern>
</encoder>
```

### 2. **Maven Configuration** (`pom.xml`)
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
</properties>
```

### 3. **Spring Boot Plugin JVM Arguments**
```xml
<jvmArguments>
    -Dfile.encoding=UTF-8
    -Dconsole.encoding=UTF-8
    -Duser.timezone=Asia/Ho_Chi_Minh
    -Djava.awt.headless=true
</jvmArguments>
```

### 4. **Application Properties**
```properties
# Console and File Encoding
logging.charset.console=UTF-8
logging.charset.file=UTF-8
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} -- %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} -- %msg%n
```

### 5. **UTF8EncodingConfig Class**
Tự động cấu hình UTF-8 system properties khi application khởi động.

## 🚀 Cách chạy ứng dụng với UTF-8

### **Windows:**
```bash
# Sử dụng script đã tạo
./run-with-utf8.bat

# Hoặc chạy manual
chcp 65001
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
mvn spring-boot:run
```

### **Linux/Mac:**
```bash
# Sử dụng script đã tạo
chmod +x run-with-utf8.sh
./run-with-utf8.sh

# Hoặc chạy manual
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export JAVA_OPTS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
mvn spring-boot:run
```

## 🧪 Test Encoding

Chạy test để verify encoding hoạt động:
```bash
mvn test -Dtest=EncodingTest
```

Test này sẽ kiểm tra:
- ✅ System charset là UTF-8
- ✅ Vietnamese characters được preserve
- ✅ Special characters và emojis
- ✅ Log messages hiển thị đúng
- ✅ File encoding properties
- ✅ Console encoding properties

## 🔍 Troubleshooting

### **Vấn đề: Console vẫn hiển thị ký tự lạ**
**Giải pháp:**
1. **Windows Command Prompt:**
   ```cmd
   chcp 65001
   ```

2. **Windows PowerShell:**
   ```powershell
   [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
   ```

3. **IDE Console (IntelliJ/Eclipse):**
   - File → Settings → Editor → File Encodings → Set to UTF-8
   - Run Configuration → VM Options: `-Dfile.encoding=UTF-8`

### **Vấn đề: Database không lưu đúng tiếng Việt**
**Giải pháp:** Đã cấu hình trong `application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=SchoolManagementDB;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=true;characterEncoding=UTF-8;loginTimeout=30
```

### **Vấn đề: Log files không hiển thị đúng**
**Giải pháp:** Logback đã được cấu hình với UTF-8 charset trong `logback-spring.xml`.

## 📊 Verification Results

Sau khi setup, bạn sẽ thấy trong logs:
```
🔧 Initializing UTF-8 system properties for Vietnamese text support
🔍 Current encoding settings:
   - file.encoding: UTF-8
   - Default charset: UTF-8
   - JVM default charset: UTF-8
   - Console encoding: UTF-8
   - Vietnamese encoding test: ✅ PASSED (Characters preserved)
🎯 UTF-8 system properties initialized successfully
```

## 🎯 Emoji Display Solution

### **LoggingUtils Class**
Để giải quyết vấn đề emoji không hiển thị đúng trên Windows console:

```java
// Sử dụng LoggingUtils thay vì emoji trực tiếp
log.info(LoggingUtils.SEARCH + " Starting verification...");
log.info(LoggingUtils.SUCCESS + " Operation completed");
log.error(LoggingUtils.ERROR + " Error occurred");
```

### **Platform-Specific Display**
- **Windows**: Text alternatives (`[SEARCH]`, `[OK]`, `[ERROR]`)
- **Unix/Linux**: Native emojis (`🔍`, `✅`, `❌`)

### **Available Constants**
```java
LoggingUtils.SEARCH    // 🔍 / [SEARCH]
LoggingUtils.CONFIG    // 🔧 / [CONFIG]
LoggingUtils.SUCCESS   // ✅ / [OK]
LoggingUtils.ERROR     // ❌ / [ERROR]
LoggingUtils.WARNING   // ⚠️ / [WARN]
LoggingUtils.INFO      // ℹ️ / [INFO]
LoggingUtils.REPORT    // 📋 / [REPORT]
LoggingUtils.TARGET    // 🎯 / [TARGET]
```

## 🎉 Kết quả

Sau khi áp dụng tất cả cấu hình trên:
- ✅ Console output hiển thị đúng tiếng Việt
- ✅ Log files lưu đúng encoding
- ✅ Database operations với tiếng Việt hoạt động
- ✅ API responses trả về đúng Unicode
- ✅ Emoji hiển thị đúng trên tất cả platforms
- ✅ Không còn ký tự lạ như "Γ£à" hay "≡ƒöì"
- ✅ Test suite pass hoàn toàn

## 📝 Lưu ý quan trọng

1. **Luôn sử dụng UTF-8** cho tất cả text files trong project
2. **Kiểm tra IDE encoding settings** để đảm bảo consistency
3. **Sử dụng scripts đã tạo** để chạy application với encoding đúng
4. **Chạy EncodingTest** định kỳ để verify encoding hoạt động
5. **Database collation** nên được set thành UTF-8 compatible
