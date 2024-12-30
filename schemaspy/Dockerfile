FROM schemaspy/schemaspy:latest

USER root
RUN addgroup -S schemaspy && adduser -S schemaspy -G schemaspy
RUN mkdir -p /drivers /output && chown -R schemaspy:schemaspy /drivers /output

RUN apk update && apk add --no-cache curl
RUN curl -fSL https://jdbc.postgresql.org/download/postgresql-42.7.4.jar -o /drivers/postgresql.jar

USER schemaspy