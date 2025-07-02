FROM schemaspy/schemaspy:latest

USER root
RUN addgroup --system schemaspy && adduser --system --ingroup schemaspy schemaspy

RUN mkdir -p /drivers /output && chown -R schemaspy:schemaspy /drivers /output

RUN apt-get update && apt-get install -y curl \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN curl -fSL https://jdbc.postgresql.org/download/postgresql-42.7.4.jar -o /drivers/postgresql.jar

USER schemaspy