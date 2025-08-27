

# report-generator

## Purpose

This application is designed to generate PDF and other report formats for wildfire prevention and fuel management, using JasperReports templates. It exposes a REST API for report generation and is intended to be used as a sidecar or microservice in larger workflows.

## JasperReports

[JasperReports](https://community.jaspersoft.com/project/jasperreports-library) is an open-source Java reporting library that enables the creation of rich, pixel-perfect documents (PDF, HTML, Excel, etc.) from Java applications. This project uses JasperReports via the Quarkus JasperReports extension to render wildfire-related reports from pre-defined `.jrxml` templates.

This project uses [Quarkus](https://quarkus.io/), the Supersonic Subatomic Java Framework, and JasperReports for report generation.

## Dependencies

The following dependencies are required to build and run this application:

- Java 21
- Maven (or use the included `mvnw` wrapper)
- Quarkus CLI (optional, for CLI usage)
- Docker (for container builds)
- GraalVM (for native builds, or use containerized build)

### Maven/Quarkus dependencies (see `pom.xml`):
- `io.quarkiverse.jasperreports:quarkus-jasperreports`
- `io.quarkus:quarkus-arc`
- `io.quarkus:quarkus-rest`
- `io.quarkus:quarkus-smallrye-openapi`
- `io.quarkus:quarkus-hibernate-orm`
- `io.quarkus:quarkus-hibernate-orm-panache`
- `io.quarkus:quarkus-jdbc-h2`
- `io.quarkus:quarkus-rest-jackson`
- `org.projectlombok:lombok` (provided)

## Building and Running with Quarkus CLI

You can use the Quarkus CLI to build and run the application:

```shell
quarkus dev
```

Or using Maven wrapper:

```shell
./mvnw quarkus:dev
```

## Packaging the Application

To package the application as a JVM jar:

```shell
./mvnw package
```

Run the packaged application:

```shell
java -jar target/quarkus-app/quarkus-run.jar
```

## Building and Running with Docker

### JVM Mode

1. Build the application jar:
	```shell
	./mvnw package
	```
2. Build the Docker image:
	```shell
	docker build -f src/main/docker/Dockerfile.jvm -t quarkus/report-generator-jvm .
	```
3. Run the Docker container:
	```shell
	docker run -i --rm -p 8080:8080 quarkus/report-generator-jvm
	```

### Native Mode

1. Build and package the native executable in a container:
	```shell
	docker build -f src/main/docker/Dockerfile.native -t report-generator-native .
	```
2. Run the native Docker container:
	```shell
	docker run -i --rm -p 8080:8080 report-generator-native
	```

## Notes

- The application exposes its API on port 8080 by default.
- For development, the Quarkus Dev UI is available at [http://localhost:8080/q/dev/](http://localhost:8080/q/dev/).
- For more information on Quarkus CLI, see [Quarkus CLI Guide](https://quarkus.io/guides/cli-tooling).
- For JasperReports usage, see [Quarkus JasperReports Guide](https://docs.quarkiverse.io/quarkus-jasperreports/dev/index.html).
