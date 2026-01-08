# Update the client (UI) service without tearing down the DB or API
$ScriptDir = Split-Path $MyInvocation.MyCommand.Path
Push-Location $ScriptDir

Write-Host "Rebuilding and updating the client service..."
docker-compose -f docker-compose.yml -f docker-compose.local.yml --profile dev up -d --build client

Pop-Location
