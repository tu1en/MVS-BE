# PowerShell script to test lecture date fix API
$baseUrl = "http://localhost:8088"

# Step 1: Login as admin
Write-Host "🔐 Logging in as admin..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✅ Login successful" -ForegroundColor Green
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Run initial verification
Write-Host "`n🔍 Running initial data verification..." -ForegroundColor Yellow
try {
    $headers = @{ Authorization = "Bearer $token" }
    $verificationResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/data-verification/run" -Method GET -Headers $headers
    
    Write-Host "📊 Initial Verification Results:" -ForegroundColor Cyan
    Write-Host "   Total Issues: $($verificationResponse.totalIssues)" -ForegroundColor White
    Write-Host "   Critical Issues: $($verificationResponse.criticalIssues)" -ForegroundColor Red
    Write-Host "   Warning Issues: $($verificationResponse.warningIssues)" -ForegroundColor Yellow
    Write-Host "   Info Issues: $($verificationResponse.infoIssues)" -ForegroundColor Blue
    
    if ($verificationResponse.hasIssues) {
        Write-Host "`n📋 Issues found:" -ForegroundColor Yellow
        foreach ($issue in $verificationResponse.issues) {
            Write-Host "   [$($issue.severity)] $($issue.code): $($issue.message)" -ForegroundColor Yellow
            if ($issue.details) {
                Write-Host "      Details: $($issue.details)" -ForegroundColor Gray
            }
        }
    }
} catch {
    Write-Host "❌ Initial verification failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Fix lecture dates
Write-Host "`n🔧 Fixing lecture dates..." -ForegroundColor Yellow
try {
    $fixResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/data-fix/fix-lecture-dates" -Method POST -Headers $headers
    Write-Host "✅ Fix result: $fixResponse" -ForegroundColor Green
} catch {
    Write-Host "❌ Fix failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 4: Run post-fix verification
Write-Host "`n🔍 Running post-fix verification..." -ForegroundColor Yellow
try {
    $postFixResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/data-verification/run" -Method GET -Headers $headers
    
    Write-Host "📊 Post-Fix Verification Results:" -ForegroundColor Cyan
    Write-Host "   Total Issues: $($postFixResponse.totalIssues)" -ForegroundColor White
    Write-Host "   Critical Issues: $($postFixResponse.criticalIssues)" -ForegroundColor Red
    Write-Host "   Warning Issues: $($postFixResponse.warningIssues)" -ForegroundColor Yellow
    Write-Host "   Info Issues: $($postFixResponse.infoIssues)" -ForegroundColor Blue
    
    if ($postFixResponse.hasIssues) {
        Write-Host "`n📋 Remaining issues:" -ForegroundColor Yellow
        foreach ($issue in $postFixResponse.issues) {
            Write-Host "   [$($issue.severity)] $($issue.code): $($issue.message)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "`n🎉 NO ISSUES FOUND! All problems resolved!" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Post-fix verification failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 5: Compare results
Write-Host "`n📊 Results Comparison:" -ForegroundColor Cyan
Write-Host "Before Fix - Total Issues: $($verificationResponse.totalIssues), Warnings: $($verificationResponse.warningIssues)" -ForegroundColor White
Write-Host "After Fix  - Total Issues: $($postFixResponse.totalIssues), Warnings: $($postFixResponse.warningIssues)" -ForegroundColor White

$issuesReduced = $verificationResponse.totalIssues - $postFixResponse.totalIssues
$warningsReduced = $verificationResponse.warningIssues - $postFixResponse.warningIssues

if ($issuesReduced -gt 0 -or $warningsReduced -gt 0) {
    Write-Host "✅ SUCCESS: Reduced $issuesReduced total issues and $warningsReduced warnings" -ForegroundColor Green
} elseif ($postFixResponse.totalIssues -eq 0) {
    Write-Host "✅ SUCCESS: All issues resolved!" -ForegroundColor Green
} else {
    Write-Host "⚠️ No improvement detected" -ForegroundColor Yellow
}

# Step 6: Health check
Write-Host "`n🏥 Final health check..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/api/admin/data-verification/health" -Method GET -Headers $headers
    Write-Host "🏥 Health Status: $healthResponse" -ForegroundColor Green
} catch {
    Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n✅ ============== TEST COMPLETED ==============" -ForegroundColor Green
