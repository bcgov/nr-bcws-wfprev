# Restore database from dump
$ScriptDir = Split-Path $MyInvocation.MyCommand.Path
Push-Location $ScriptDir

Write-Host "Restoring database 'wfprev' from dump..."
docker exec -i wfprev-postgres pg_restore -U wfprev -d wfprev --data-only --disable-triggers /dump/wfprev.dump

if ($LASTEXITCODE -eq 0) {
    Write-Host "Database restoration completed successfully." -ForegroundColor Green
} else {
    Write-Error "Database restoration failed."
}

Pop-Location
