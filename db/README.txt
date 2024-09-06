To build a local instance of Postgis and create the model from the liquibase change log:

// pull the postgis image
docker pull postgis/postgis:16-3.4

// run it
docker run --name wfprev-postgres -e POSTGRES_USER=wfprev -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgis/postgis:16-3.4


//If you want to mount a data directory, add this additional config:
```
-v /data:/var/lib/postgresql/data
```

// to find the IP address
docker ps
docker inspect <container id>
~ or ~
docker exec -it <container name> /bin/bash
ip -4 -o address
exit

// then create the liquibase image and run that

docker build -t liquibase -f Dockerfile.liquibase.local .
docker run --rm liquibase --url=jdbc:postgresql://172.17.0.2:5432/wfprev --changelog-file=main-changelog.json --username=wfprev --password=password update

// Data load coming soon...