

# report-generator

## Purpose

This application is designed to generate PDF and other report formats for wildfire prevention and fuel management, using JasperReports templates. It exposes a REST API for report generation and is intended to be used as a sidecar or microservice in larger workflows.


## JasperReports

[JasperReports](https://community.jaspersoft.com/project/jasperreports-library) is an open-source Java reporting library that enables the creation of rich, pixel-perfect documents (PDF, HTML, Excel, etc.) from Java applications. This project uses JasperReports via the Quarkus JasperReports extension to render wildfire-related reports from pre-defined `.jrxml` templates.

## AWS Lambda Usage


### Lambda Input Model

The Lambda handler expects an input JSON payload structured as follows:

```
{
	"reports": [
		{
			"reportType": "XLSX",
			"reportName": "culture-prescribed-fire-report",
			"xlsxReportData": {
				"culturePrescribedFireReportData": [
					{ /* fields for CulturePrescribedFireReportData */ }
				],
				"fuelManagementReportData": [
					{ /* fields for FuelManagementReportData */ }
				]
			}
		},
		{
			"reportType": "XLSX",
			"reportName": "fuel-management-report",
			"xlsxReportData": {
				"fuelManagementReportData": [
					{ /* fields for FuelManagementReportData */ }
				]
			}
		}
	]
}
```

- `reports`: Array of report objects to generate. Each report produces a separate XLSX file in the output.
- `reportType`: Type of report (currently only `XLSX` is supported).
- `reportName`: Used as the output filename (e.g., `culture-prescribed-fire-report.xlsx`).
- `xlsxReportData`: Contains lists of report data objects for each supported sheet type.
	- `culturePrescribedFireReportData`: List of objects for the "Culture Prescribed Fire" sheet.
	- `fuelManagementReportData`: List of objects for the "Fuel Management" sheet.

See the model classes in `src/main/java/ca/bc/gov/nrs/reportgenerator/model/` for all available fields.

Build with `Dockerfile.lambda` for AWS Lambda deployment.

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

## Building and Running for AWS Lambda

1. Build the native Lambda binary in a container:
	```shell
	docker build -f src/main/docker/Dockerfile.lambda -t report-generator-lambda .
	```
2. Deploy the resulting image to AWS Lambda (see AWS docs for container deployment).

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

## Testing

Unit tests for Lambda and REST endpoints are in `src/test/java/ca/bc/gov/nrs/reportgenerator/`. Run tests with:
```shell
./mvnw test
```

## Troubleshooting: Downloading XLSX from Lambda Response
When invoking the Lambda handler (e.g., via Postman, AWS Console, or API Gateway), the response is a JSON object with the following structure (for multiple files):

```
{
	"headers": { ... },
	"isBase64Encoded": true,
	"files": [
		{
			"filename": "culture-prescribed-fire-report.xlsx",
			"content": "<base64-encoded-xlsx>"
		},
		{
			"filename": "fuel-management-report.xlsx",
			"content": "<base64-encoded-xlsx>"
		}
	],
	"statusCode": 200
}
```

**Note:** Postman 'Send and Download' will save this JSON as a text file, not as XLSX files.

To extract the actual XLSX files:

1. For each object in the `files` array, copy the value of the `content` field (the base64 string).
2. Save it to a file (e.g., `output1.b64`, `output2.b64`).
3. Decode each to binary and save as `.xlsx` using the corresponding `filename`:

### PowerShell
```powershell
$b64 = Get-Content output1.b64
[System.IO.File]::WriteAllBytes('culture-prescribed-fire-report.xlsx', [Convert]::FromBase64String($b64))
# Repeat for output2.b64 and fuel-management-report.xlsx
```

### Linux/macOS
```sh
cat output1.b64 | base64 -d > culture-prescribed-fire-report.xlsx
cat output2.b64 | base64 -d > fuel-management-report.xlsx
```

You can now open the `.xlsx` files in Excel or other spreadsheet tools.

**Tip:** For automated testing, use a script to extract and decode all files from the Lambda JSON response.

