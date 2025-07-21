# PowerShell script để test các endpoint cho STUDENT role
# Chạy script này sau khi ứng dụng Spring Boot đã khởi động

$baseUrl = "http://localhost:8088"

Write-Host "🔍 Testing STUDENT role endpoints..." -ForegroundColor Green

# Test 1: Debug student data for student ID 1
Write-Host "`n1. Testing debug endpoint for student ID 1..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/debug/student/1" -Method GET
    Write-Host "✅ Debug endpoint response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Debug endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Test new student conversations endpoint
Write-Host "`n2. Testing student conversations endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/student-messages/student/1/conversations" -Method GET
    Write-Host "✅ Student conversations endpoint response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Student conversations endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Test student assignments
Write-Host "`n3. Testing student assignments..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/assignments/student/1" -Method GET
    Write-Host "✅ Student assignments response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 2
} catch {
    Write-Host "❌ Student assignments failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Test student classrooms
Write-Host "`n4. Testing student classrooms..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/classrooms/student/1" -Method GET
    Write-Host "✅ Student classrooms response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 2
} catch {
    Write-Host "❌ Student classrooms failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Test student messages
Write-Host "`n5. Testing student messages..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/student-messages/student/1" -Method GET
    Write-Host "✅ Student messages response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 2
} catch {
    Write-Host "❌ Student messages failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🏁 Testing completed!" -ForegroundColor Green
