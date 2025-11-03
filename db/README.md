# WFPrev Local PostGIS Setup

This guide will help you build a local instance of PostGIS, set up the database, and apply the model using Liquibase from the change log.

## Prerequisites

- Docker installed on your local machine.
- `Dockerfile.liquibase.local`, `main-changelog.json`, and the `scripts` folder available in your repository.

## Project Structure

- **`main-changelog.json`**: The main Liquibase changelog file, which includes the following changes:
  - Creates logins using SQL scripts.
  - Creates roles.
  - Creates the `wfprev` schema.
  - Adds required extensions.
- **`Dockerfile.liquibase.local`**: Dockerfile for running Liquibase commands.
- **`scripts` folder**: Contains the SQL scripts for various database changes as referenced in `main-changelog.json`.

## Step 1: Pull and Run PostGIS

1. **Pull the PostGIS Image**:

   ```bash
   docker pull postgis/postgis:16-3.4

2. **Run the PostGIS Containe**:

   ```bash
   docker run --name wfprev-postgres -e POSTGRES_USER=wfprev -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgis/postgis:16-3.4

## Step 2: Find the IP Address

1. **Get the Container ID:** :
    docker ps

2. **Inspect the Container for IP Address:** :
    docker inspect <container_id>

## Step 3: Set Up and Run Liquibase

1. **Build the Liquibase Docker Image** :
Create a Dockerfile.liquibase.local with the following content:

FROM liquibase/liquibase:5.0
RUN lpm add postgresql --global
COPY ./scripts ./scripts
COPY ./main-changelog*.json .
COPY ./liquibase.properties .

ENTRYPOINT [ "sh", "-c", "liquibase $COMMAND $TARGET_LIQUIBASE_TAG --changelog-file=$CHANGELOG_FILE -Dschemaname=$SCHEMA_NAME" ]

Build the Liquibase Docker image:
docker build -t liquibase -f Dockerfile.liquibase.local .

2. ** Run Liquibase Update:**:

docker run --rm liquibase \
    --url=jdbc:postgresql://<your_postgis_ip>:5432/wfprev \
    --changelog-file=main-changelog.json \
    --username=wfprev \
    --password=password \
    update
