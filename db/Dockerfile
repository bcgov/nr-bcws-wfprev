FROM liquibase/liquibase

COPY ./scripts ./scripts
COPY ./main-changelog*.json .
COPY ./liquibase.properties .

ENTRYPOINT [ "sh", "-c", "liquibase $COMMAND $TARGET_LIQUIBASE_TAG --changelog-file=$CHANGELOG_FILE -Dschemaname=$SCHEMA_NAME"]