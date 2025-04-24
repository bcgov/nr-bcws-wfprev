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

## Local Deployment

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



[![Lifecycle:Experimental](https://img.shields.io/badge/Lifecycle-Experimental-339999)](<Redirect-URL>)

