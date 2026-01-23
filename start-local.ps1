param (
    [switch]$Clean
)

# Start Docker Compose with local override
$ScriptDir = Split-Path $MyInvocation.MyCommand.Path
Push-Location $ScriptDir

if ($Clean) {
    Write-Host "Cleaning volumes requested. Running stop-local.ps1 -ClearVolumes..."
    ./stop-local.ps1 -ClearVolumes
    $env:SKIP_RESTORE = "true"
} else {
    $env:SKIP_RESTORE = "false"
}

# Build the API locally
Push-Location server/wfprev-api
Write-Host "Building API locally..."
./mvnw clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed!"
    Pop-Location
    exit 1
}
Pop-Location

# Remove orphans to clean up potential old service states
docker-compose -f docker-compose.yml -f docker-compose.local.yml --profile dev up -d --build --remove-orphans
Pop-Location
