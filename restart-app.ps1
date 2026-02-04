# Kill any process using port 7777
$port = 7777
$connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if ($connections) {
    $connections | ForEach-Object {
        $processId = $_.OwningProcess
        Stop-Process -Id $processId -Force
        Write-Host "✓ Killed process $processId using port $port" -ForegroundColor Green
    }
    Start-Sleep -Seconds 2
} else {
    Write-Host "✓ Port $port is free" -ForegroundColor Green
}

# Start the application
Write-Host "`nStarting AlbaNet application on port $port..." -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop the application`n" -ForegroundColor Yellow

.\mvnw.cmd spring-boot:run
