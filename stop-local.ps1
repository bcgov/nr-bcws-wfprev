param (
    [switch]$ClearVolumes
)

# Stop Docker Compose with local override
$ScriptDir = Split-Path $MyInvocation.MyCommand.Path
Push-Location $ScriptDir

$command = "docker-compose -f docker-compose.yml -f docker-compose.local.yml --profile dev down"

if ($ClearVolumes) {
    Write-Host "Stopping and clearing volumes..."
    $command += " -v"
} else {
    Write-Host "Stopping..."
}

Invoke-Expression $command

Pop-Location
