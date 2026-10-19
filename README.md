# nr-bcws-wfprev
The goal of the BC Wildfire Service (BCWS) Prevention Program is to reduce the negative impacts of wildfire on public safety, property, the environment and the economy using the seven disciplines of the FireSmart program.

## Technologies used

* [Angular](https://angular.io/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* [PostGIS](https://postgis.net/)
* [Terraform](https://www.terraform.io)
* [Terragrunt](https://terragrunt.gruntwork.io)
* [AWS](https://aws.amazon.com/)
* [Docker](https://www.docker.com/)

# Getting Started

## Improved Local Development (Recommended)

For a streamlined setup including automated builds and data restoration, use the unified shell scripts in the [docker](./docker) folder.

### macOS, Linux & Windows (via Git Bash / WSL)
```bash
# 1. Grant execution permissions
chmod +x docker/*.sh

# 2. Start the local stack and restore seed data
./docker/start-local.sh -r
```

See the [Local Dev README](./docker/README.md) for full prerequisites and Windows setup instructions.

## Manual Local Deployment

### The Easy Way

You can start the full system with the following command:

* Create a .env file in the root of the project with the following content:
```WFPREV_DATASOURCE_URL=jdbc:postgresql://localhost:5432/wfprev
WFPREV_DATASOURCE_USERNAME=wfprev
WFPREV_DATASOURCE_PASSWORD=***
WFPREV_CLIENT_ID=WFNEWS-REST
WFPREV_CLIENT_SECRET=***
WEBADE_OAUTH2_CHECK_TOKEN_URL=https://wfappsi.nrs.gov.bc.ca/pub/oauth2/v1/check_token
WEBADE_OAUTH2_CHECK_AUTHORIZE_URL=https://wfappsi.nrs.gov.bc.ca/ext/oauth2/v1/oauth/authorize
SPRING_MAIN_ALLOW_BEAN_DEFINITION_OVERRIDING=true
WFPREV_DB_PASSWORD=***
POSTGRES_PASSWORD=**
WFPREV_BASE_URL=http://localhost:8080
WFPREV_GDB_FUNCTION_NAME=wfprev-gdb-dev
```

* Then run this command```docker compose up```
    * This will start the database, liquibase and the backend service
    * If you want to also run the API (With GraalVM), run the following command:
    ```docker compose --profile api up```
    * to get GraalVM re-register, run :
    ```docker compose --profile api up --build```
    * To wipe out the db on your local
    ```docker compose down -v ```
* NOTE - Windows users may have to fix the line endings in the server/wfprev-api/mvnw file.  The simplest way to do this is to run the following command:
```
Open the mvnw file in a text editor (like VS Code or Notepad++).
Convert the line endings:
  •	VS Code: Look for the line ending selector in the bottom-right corner and change it to LF.
  •	Notepad++: Use the menu Edit > EOL Conversion > Unix (LF).
```
### The Hard Way

You can create a database instance via

#### Postgres
```
docker pull postgis/postgis:16-3.4
docker run --name wfprev-postgres \
    --env-file .env.local \
    -e POSTGRES_USER=wfprev \
    -p 5432:5432 \
    -d postgis/postgis:16-3.4

```

Note: Mac users will get the following error, jsut ignore for now ```WARNING: The requested image's platform (linux/amd64) does not match the detected host platform (linux/arm64/v8) and no specific platform was requested
f0de92debad131b48e2d72a9d211bafaa2b8bcb800e5077bb59f3225e5729086```

And build the database model with Liquibase:

#### Liquibase
```
cd db
docker build -t liquibase -f Dockerfile.liquibase.local .   
docker run --rm \
    --env-file .env.local \
    liquibase \
    /bin/bash -c 'liquibase \
    --url=jdbc:postgresql://host.docker.internal:5432/wfprev \
    --changelog-file=main-changelog.json \
    --username=wfprev \
    --password=$WFPREV_DB_PASSWORD \
    update'
```

The db/.env.local file should have the following content:

```
WFPREV_DB_PASSWORD=***
POSTGRES_PASSWORD=***
```

#### Backend
We typically run the API from our IDE but you can find more detailed information in the README at server/wfprev-api

# Releases

Deployments to TEST and PROD happen when you publish a GitHub release. The release type and the tag name decide where it goes:

| Release type | Workflow | Tag contains | Deploys to |
|---|---|---|---|
| Pre-release | [prerelease.yml](.github/workflows/prerelease.yml) | `-rc` | TEST (`wftst`) |
| Full release | [release.yml](.github/workflows/release.yml) | `-release` | PROD (`wfprd`) |

Neither workflow builds anything. Each one finds the API, Liquibase, GDB extractor and report generator images tagged with the commit SHA the release tag points at, re-tags them with the release tag (GHCR and ECR), then runs Terragrunt apply with Liquibase and builds the UI. **The images must already exist for that exact commit.**

## Before you release: build images for the commit

- **Commits on `main`:** merging a PR runs `build-full-environment`, so the images already exist.
- **Commits on a release branch (e.g. `release/1.4.0`):** merging PRs into it does not build anything. Go to Actions → **build-full-environment** → Run workflow, pick the release branch and wait for it to go green.
  - This also deploys the branch to INT as `latest`. To leave INT alone, run these on the branch instead: `mvn-build` (`wfprev-api`, `server`), `liquibase-build`, `node-build` (`wfprev-gdb-extractor`) and `report-generator-image-build`.

## Release to TEST

1. Check that the image build above has finished for the commit you're releasing (`git fetch && git rev-parse origin/release/1.4.0`).
2. Go to GitHub → **Releases** → **Draft a new release**.
3. **Choose a tag:** type a new tag following the existing pattern, e.g. `v1.4.0-rc2` (`v1.4.0-rc1.3` for a small re-spin). Set **Target** to the release branch.
4. **Title:** the tag name. Click **Generate release notes** with the previous tag set to the last rc.
5. Tick **Set as a pre-release**.
6. Click **Publish release** and follow the `prerelease` run in Actions: `retag-images` → `deploy-wftst` → `wfprev-ui-tst`.
7. Smoke-test TEST.

If a job fails for a transient reason, use **Re-run failed jobs** on that run rather than deleting and recreating the release.

## Release to PROD

1. Find the rc that passed testing (e.g. `v1.4.0-rc2`). PROD must get that exact commit, so don't target the branch head if anything has merged since.
2. Create the prod tag on the same commit and push it (pushing a tag doesn't start any workflow):
   ```bash
   git fetch --tags
   git tag v1.4.0-release v1.4.0-rc2^{commit}
   git push origin v1.4.0-release
   ```
3. Go to **Releases** → **Draft a new release** and choose the existing tag `v1.4.0-release`.
4. **Title:** the version (e.g. `v1.4.0`). Generate release notes with the previous tag set to the last prod tag (e.g. `v1.3.0-release`) so the notes cover the whole release.
5. Leave **Set as a pre-release** unticked and **Set as the latest release** ticked.
6. Click **Publish release** and follow the `release` run in Actions: `check-images` → `retag-images` + `validate-images` → `deploy-prod` → `wfprev-ui`.
7. Smoke-test PROD, then merge the release branch back to `main` if it has fixes `main` doesn't.

## Release gotchas

- **Publishing is the deploy.** `wfprd` has no required-reviewer approval, so nothing pauses the run once you publish. Use **Save draft** if someone needs to review it first; drafts don't start anything.
- **Don't promote an rc by unticking "pre-release".** That fires `released` with an `-rc` tag: the images get re-tagged but `deploy-prod` is skipped, so nothing reaches PROD and the rc becomes "Latest". Always create a new `-release` tag.
- **The prod tag must contain `-release`.** A plain `v1.4.0` tag won't deploy.
- **Liquibase runs against PROD and changesets can't be undone.** Make sure there's a recent RDS snapshot first.
- **Editing a release does nothing.** Neither workflow listens for edits, so changing the notes or target later won't redeploy.


[![Lifecycle:Experimental](https://img.shields.io/badge/Lifecycle-Experimental-339999)](<Redirect-URL>)

