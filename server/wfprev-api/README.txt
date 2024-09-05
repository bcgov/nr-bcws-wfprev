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