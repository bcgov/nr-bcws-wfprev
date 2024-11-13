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

## Getting Started

### Local Deployment

For local development, we recommend starting individual services with Docker

You can create a database instance via

```
docker pull postgis/postgis:16-3.4
docker run --name wfprev-postgres -e POSTGRES_USER=wfprev -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgis/postgis:16-3.4

```

Note: Mac users will get the following error, jsut ignore for now ```WARNING: The requested image's platform (linux/amd64) does not match the detected host platform (linux/arm64/v8) and no specific platform was requested
f0de92debad131b48e2d72a9d211bafaa2b8bcb800e5077bb59f3225e5729086```

And build the database model with Liquibase:

```
cd db
docker build -t liquibase -f Dockerfile.liquibase.local .   
docker run --rm liquibase \
    --url=jdbc:postgresql://host.docker.internal:5432/wfprev \
    --changelog-file=main-changelog.json \
    --username=wfprev \
    --password=password \
    update
```
[![Lifecycle:Experimental](https://img.shields.io/badge/Lifecycle-Experimental-339999)](<Redirect-URL>)
