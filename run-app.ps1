# PowerShell script để chạy Spring Boot application với seeders
# Chạy với: .\run-app.ps1

Write-Host "🚀 Starting Spring Boot Application with Seeders..." -ForegroundColor Green
Write-Host ""

# Set UTF-8 encoding
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Check if Java is available
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java not found! Please install Java 17 or higher." -ForegroundColor Red
    exit 1
}

# Clean and build
Write-Host "📦 Building application..." -ForegroundColor Yellow
try {
    mvn clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Build failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Build successful!" -ForegroundColor Green
} catch {
    Write-Host "❌ Build error: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🔄 Starting application with local profile..." -ForegroundColor Yellow
Write-Host "📊 Seeders will run automatically to populate database" -ForegroundColor Cyan
Write-Host "⏳ Please wait for application to start (may take 30-60 seconds)..." -ForegroundColor Cyan
Write-Host ""

# Run application
try {
    java -jar target/classroom-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local --server.port=8080
} catch {
    Write-Host "❌ Application failed to start: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🛑 Application stopped." -ForegroundColor Yellow
