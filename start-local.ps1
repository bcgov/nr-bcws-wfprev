param (
    [switch]$Clean,
    [switch]$RestoreData
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

if ($RestoreData) {
    Write-Host "Data restore requested. Waiting for Liquibase to complete migrations..."
    
    # Wait for liquibase container to exit and get its exit code
    # This command blocks until the container stops
    $exitCode = docker wait nr-bcws-wfprev-liquibase-1

    if ($exitCode -ne 0) {
        Write-Error "Liquibase migrations failed with exit code $exitCode."
        Pop-Location
        exit 1
    }

    Write-Host "Liquibase finished (0). Restoring data from dump..."
    # Run pg_restore in data-only mode
    docker exec -i wfprev-postgres pg_restore -U wfprev -d wfprev --data-only --disable-triggers /dump/wfprev.dump
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Data restoration complete!" -ForegroundColor Green
    } else {
        Write-Warning "Data restoration completed with warnings (this is common for pg_restore with existing schemas)."
    }
}

Pop-Location
