Write-Host "Testing Lecture Date Fix API..." -ForegroundColor Green

# Step 1: Login
Write-Host "Step 1: Login as admin..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8088/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token
Write-Host "Login successful. Token: $($token.Substring(0,20))..." -ForegroundColor Green

# Step 2: Initial verification
Write-Host "Step 2: Initial verification..." -ForegroundColor Yellow
$headers = @{ Authorization = "Bearer $token" }
$initialResult = Invoke-RestMethod -Uri "http://localhost:8088/api/admin/data-verification/run" -Method GET -Headers $headers
Write-Host "Before Fix - Total Issues: $($initialResult.totalIssues), Warnings: $($initialResult.warningIssues)" -ForegroundColor Cyan

# Step 3: Fix lecture dates
Write-Host "Step 3: Fixing lecture dates..." -ForegroundColor Yellow
$fixResult = Invoke-RestMethod -Uri "http://localhost:8088/api/admin/data-fix/fix-lecture-dates" -Method POST -Headers $headers
Write-Host "Fix result: $fixResult" -ForegroundColor Green

# Step 4: Post-fix verification
Write-Host "Step 4: Post-fix verification..." -ForegroundColor Yellow
$postResult = Invoke-RestMethod -Uri "http://localhost:8088/api/admin/data-verification/run" -Method GET -Headers $headers
Write-Host "After Fix - Total Issues: $($postResult.totalIssues), Warnings: $($postResult.warningIssues)" -ForegroundColor Cyan

# Results
Write-Host "Results:" -ForegroundColor Green
$issuesReduced = $initialResult.totalIssues - $postResult.totalIssues
$warningsReduced = $initialResult.warningIssues - $postResult.warningIssues
Write-Host "Issues reduced: $issuesReduced" -ForegroundColor White
Write-Host "Warnings reduced: $warningsReduced" -ForegroundColor White

if ($postResult.totalIssues -eq 0) {
    Write-Host "SUCCESS: All issues resolved!" -ForegroundColor Green
} elseif ($issuesReduced -gt 0) {
    Write-Host "SUCCESS: Reduced $issuesReduced issues!" -ForegroundColor Green
} else {
    Write-Host "No improvement detected" -ForegroundColor Yellow
}

Write-Host "Test completed!" -ForegroundColor Green
