# Simple PowerShell script to test lecture date fix API
$baseUrl = "http://localhost:8088"

Write-Host "🔐 Step 1: Login as admin..." -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"password123"}'
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token
Write-Host "✅ Login successful" -ForegroundColor Green

Write-Host "`n🔍 Step 2: Initial verification..." -ForegroundColor Yellow
$headers = @{ Authorization = "Bearer $token" }
$initialResult = Invoke-RestMethod -Uri "$baseUrl/api/admin/data-verification/run" -Method GET -Headers $headers
Write-Host "📊 Before Fix - Total Issues: $($initialResult.totalIssues), Warnings: $($initialResult.warningIssues)" -ForegroundColor Cyan

Write-Host "`n🔧 Step 3: Fixing lecture dates..." -ForegroundColor Yellow
$fixResult = Invoke-RestMethod -Uri "$baseUrl/api/admin/data-fix/fix-lecture-dates" -Method POST -Headers $headers
Write-Host "✅ Fix result: $fixResult" -ForegroundColor Green

Write-Host "`n🔍 Step 4: Post-fix verification..." -ForegroundColor Yellow
$postResult = Invoke-RestMethod -Uri "$baseUrl/api/admin/data-verification/run" -Method GET -Headers $headers
Write-Host "📊 After Fix - Total Issues: $($postResult.totalIssues), Warnings: $($postResult.warningIssues)" -ForegroundColor Cyan

Write-Host "`n📊 Results:" -ForegroundColor Green
$issuesReduced = $initialResult.totalIssues - $postResult.totalIssues
$warningsReduced = $initialResult.warningIssues - $postResult.warningIssues
Write-Host "   Issues reduced: $issuesReduced" -ForegroundColor White
Write-Host "   Warnings reduced: $warningsReduced" -ForegroundColor White

if ($postResult.totalIssues -eq 0) {
    Write-Host "`n🎉 SUCCESS: All issues resolved!" -ForegroundColor Green
} elseif ($issuesReduced -gt 0) {
    Write-Host "`n✅ SUCCESS: Reduced $issuesReduced issues!" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ No improvement detected" -ForegroundColor Yellow
}

Write-Host "`n✅ Test completed!" -ForegroundColor Green
