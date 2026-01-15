# Restore database from dump
$ScriptDir = Split-Path $MyInvocation.MyCommand.Path
Push-Location $ScriptDir

Write-Host "Restoring database 'wfprev' from dump..."
docker exec wfprev-postgres bash /docker-entrypoint-initdb.d/restore_dump.sh

if ($LASTEXITCODE -eq 0) {
    Write-Host "Database restoration completed successfully." -ForegroundColor Green
} else {
    Write-Error "Database restoration failed."
}

Pop-Location
