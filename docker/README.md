# Local Development Scripts

This folder contains a set of unified Bash (`.sh`) orchestration scripts designed to streamline local development for the BCWS Prevention project across **Windows** (via Git Bash / WSL), **macOS**, and **Linux**.

## Key Features
- **Mock Authentication**: Supports full offline development using a mock token system (no IDIR login required).
- **Automated Database Restore**: Scripts to restore a full database dump into the local container.
- **Optimized Builds**: Fast Docker builds with correct build context and configurations.
- **Service Orchestration**: Use simple scripts to start and stop the entire development stack.

---

## Prerequisites

To run these scripts, you must have the following installed:

1. **Docker Desktop / Engine**: Ensure Docker is running.
2. **Java 21**: Required for local Maven builds (optional if reusing existing builds, see below).
3. **Maven**: The scripts automatically use the included `./mvnw` wrapper.
4. **wfprev.dump**: Place a `wfprev.dump` file inside a `wfprev.dump/` directory in the project root.
5. **Unix Shell Environment (For Windows Developers)**:
   - **Git Bash** (installed by default with Git for Windows) OR
   - **WSL** (Windows Subsystem for Linux) with Docker WSL Integration enabled.
   - **dos2unix** (highly recommended for Windows users to prevent line ending parse errors).

---

## Scripts Overview

### [start-local.sh](./start-local.sh)
The primary script for starting the local environment.
- **Usage**: `./docker/start-local.sh [options]`
- **Options**:
    - `-c` or `--clean`: Clears PostgreSQL volumes before starting to ensure a fresh setup.
    - `-r` or `--restore-data`: Automatically waits for Liquibase migrations to complete and restores the seed data.
    - `-s` or `--static`: Starts the Angular UI in **Static Runner Mode** instead of Watch Mode. (By default, the script starts the stack in Watch / Live Reload Mode).
- **Robust Java Fallback**: If JDK 21 is not installed in your active shell (common in default WSL shells), the script gracefully prints a warning and automatically locates and re-uses the pre-compiled `wfprev-api-1.0.0-SNAPSHOT.jar` target instead of exiting!

### [stop-local.sh](./stop-local.sh)
Cleanly stops the local Docker stack.
- **Usage**: `./docker/stop-local.sh [options]`
- **Options**:
    - `-v` or `--clear-volumes`: Removes the PostgreSQL data volume to clear DB state completely.

### [restore-local-db.sh](./restore-local-db.sh)
Manually triggers a data-only pg_restore from `./wfprev.dump/wfprev.dump` into a running database container.
- **Usage**: `./docker/restore-local-db.sh`

### [update-local-client.sh](./update-local-client.sh)
Rapidly rebuilds and restarts only the `client` (Angular UI) service. Useful for testing UI changes without restarting the database or API.
- **Usage**: `./docker/update-local-client.sh`

---

## Running on Windows (WSL & Git Bash)

If you are developing on Windows, you must run these scripts using **WSL** or **Git Bash**:

### A. Running in WSL (Windows Subsystem for Linux)
1. **Enable WSL Integration**: Open *Docker Desktop* on Windows -> *Settings* -> *Resources* -> *WSL Integration*, check your Linux distro, and apply.
2. **Open WSL Terminal**: Start your Linux shell (e.g., Ubuntu).
3. **Navigate to Project**: `cd /mnt/c/path/to/nr-bcws-wfprev` (replace with your path).
4. **Fix Line Endings (CRLF to LF)**: Windows Git checkout might use CRLF line endings, which causes Bash to crash. Convert them by running:
   ```bash
   sudo apt update && sudo apt install -y dos2unix
   dos2unix docker/*.sh
   ```
5. **Make Executable & Run**:
   ```bash
   chmod +x docker/*.sh
   ./docker/start-local.sh -r
   ```

### B. Running in Git Bash
1. **Open Git Bash**: Open a Git Bash terminal in the project root directory.
2. **Make Executable & Run**:
   ```bash
   chmod +x docker/*.sh
   ./docker/start-local.sh -r
   ```

---

## Shared Assets

### [db-init/](./db-init/)
Contains internal database initialization scripts used by the PostgreSQL container.
- **restore_dump.sh**: Automatically runs inside the container on first boot to restore the database if a dump is present.

## Watch / Live Reload Mode (Development)

By default, the local environment starts in **Watch / Live Reload Mode** for the Angular frontend:
1. This runs `client-watch` (instead of static assets) using a dedicated containerized Angular Dev Server configuration.
2. The host folder `./client/wfprev-war/src/main/angular` is dynamically mounted into the container. Any edits you make on your host machine will compile in milliseconds and automatically trigger a browser refresh!
3. **Mock Login Safety:** The mock login screen (`login-mock.html`) resides inside `./docker` and is mounted dynamically during watch sessions under the `"local"` Angular build configuration. This ensures that the mock login screen is never bundled or accessible in other build targets (like Staging or Production).
4. **Static Mode Alternative:** If you want to run the static production-like bundle of the client inside the container, pass the `-s` or `--static` flag when starting (e.g., `./docker/start-local.sh -s`).

---

## Local Authentication (No Login)

The local environment is configured to use a **Mock Token** by default. 
- The Frontend `TokenService` automatically injects a `mock_token` when running in `local` mode.
- The Backend validates this token against `assets/data/checktoken-user.json` served by the client container.
- This allows you to bypass the IDIR login screen and work entirely offline.

To customize user permissions, modify:
`client/wfprev-war/src/main/angular/src/assets/data/checktoken-user.json`

---

## Quick Start

```bash
# 1. Grant execution permissions
chmod +x docker/*.sh

# 2. Boot the stack with data restoration and live reloading (Watch mode is default)
./docker/start-local.sh -r
```
