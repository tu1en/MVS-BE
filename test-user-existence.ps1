# Test script to check if users exist in database
$response = try {
    Invoke-RestMethod -Uri "http://localhost:8088/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
} catch {
    $_.Exception.Response
}

Write-Host "Admin login response: $response"

$response2 = try {
    Invoke-RestMethod -Uri "http://localhost:8088/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"teacher","password":"teacher123"}'
} catch {
    $_.Exception.Response
}

Write-Host "Teacher login response: $response2"

$response3 = try {
    Invoke-RestMethod -Uri "http://localhost:8088/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"student","password":"student123"}'
} catch {
    $_.Exception.Response
}

Write-Host "Student login response: $response3"