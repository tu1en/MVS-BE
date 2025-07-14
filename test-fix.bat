@echo off
echo Testing Lecture Date Fix API...

echo.
echo Step 1: Login as admin...
curl -s -X POST http://localhost:8088/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"password123\"}" > login_response.json

echo.
echo Step 2: Extract token...
for /f "tokens=2 delims=:" %%a in ('findstr "token" login_response.json') do set TOKEN_PART=%%a
set TOKEN=%TOKEN_PART:"=%
set TOKEN=%TOKEN:}=%
set TOKEN=%TOKEN: =%

echo Token extracted: %TOKEN:~0,20%...

echo.
echo Step 3: Run initial verification...
curl -s -H "Authorization: Bearer %TOKEN%" http://localhost:8088/api/admin/data-verification/run > initial_verification.json
echo Initial verification completed

echo.
echo Step 4: Fix lecture dates...
curl -s -X POST -H "Authorization: Bearer %TOKEN%" http://localhost:8088/api/admin/data-fix/fix-lecture-dates > fix_result.json
echo Fix completed

echo.
echo Step 5: Run post-fix verification...
curl -s -H "Authorization: Bearer %TOKEN%" http://localhost:8088/api/admin/data-verification/run > post_verification.json
echo Post-fix verification completed

echo.
echo Results:
echo Initial verification:
type initial_verification.json
echo.
echo Fix result:
type fix_result.json
echo.
echo Post-fix verification:
type post_verification.json

echo.
echo Cleaning up temporary files...
del login_response.json initial_verification.json fix_result.json post_verification.json

echo.
echo Test completed!
