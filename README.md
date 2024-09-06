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

And build the database model with Liquibase:

```
docker build -t liquibase -f Dockerfile.liquibase.local .
docker run --rm liquibase --url=jdbc:postgresql://172.17.0.2:5432/wfprev --changelog-file=main-changelog.json --username=wfprev --password=password update
```
