@echo off
echo Setting up UTF-8 encoding for Spring Boot application...

REM Set console code page to UTF-8
chcp 65001

REM Set environment variables for UTF-8 encoding
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Ho_Chi_Minh -Djava.awt.headless=true
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8

echo Console encoding set to UTF-8
echo Java encoding options: %JAVA_OPTS%
echo Maven encoding options: %MAVEN_OPTS%

echo.
echo Starting Spring Boot application with UTF-8 encoding...
mvn spring-boot:run

pause
