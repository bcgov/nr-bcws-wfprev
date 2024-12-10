To locally test the build:

Ensure local db instance is running in docker (refer to README.txt in database folder)

Find container ID of local db instance;

```
docker ps -a
```

Then, inspect container to find its IP address;

```
docker inspect [container ID]
```

When IP address of your local db instance is found, 
replace localhost hostname in spring.application.datasource.url property 
of application.yaml file. E.g jdbc:postgresql://172.17.0.3:5432/wfprev

Now build the API using Maven;

```
mvn clean install (option -DskipTests if you want to ignore tests)
```

Then, once the build is complete, to locally dockerize API;

```
docker build -t wfprev-api .
docker run -p 1338:8080 wfprev-api
```

## To use the GraalVM Docker Build

1. Create a '.env' file in `server/wfprev-api` with the following content:

```WFPREV_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/wfprev
WFPREV_DATASOURCE_USERNAME=wfprev
WFPREV_DATASOURCE_PASSWORD=xxx
WFPREV_CLIENT_ID=WFNEWS-REST
WFPREV_CLIENT_SECRET=yyy
WFPREV_BASE_URL=http://localhost:4200
WEBADE_OAUTH2_CHECK_TOKEN_URL=https://<domain>/pub/oauth2/v1/check_token
WEBADE_OAUTH2_CHECK_AUTHORIZE_URL=https://<domain>/ext/oauth2/v1/oauth/authorize
```
2. Run the following command to build the GraalVM Docker image:

```bash 
 docker build -t wfprev-api -f Dockerfile.graalvm .
```

3. Run the following command to start the GraalVM Docker container:

```bash
docker run -p 8080:8080 --env-file .env wfprev-api
```

### To test the API (locally)
 1. Retrieve a token from the WebADE server using the following command:
 
 ```bash
curl --location 'https://<domain>/pub/oauth2/v1/oauth/token?disableDeveloperFilter=true&response_type=token&grant_type=client_credentials' \
--header 'Authorization: Basic xxx=' \
--header 'Cookie: ROUTEID=.1'
```
2. Use the token to access the API:

```bash
curl --location 'http://localhost:8080/' \
--header 'Authorization: Bearer <value returned from step 1>'
```

