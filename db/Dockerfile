FROM liquibase/liquibase

COPY ./scripts ./scripts
COPY ./*changelog.json .
COPY ./liquibase.properties .
COPY ./executeLiquibase.sh .
run chmod +x executeLiquibase.sh

# Old command replaced by command below to run two changelog files
#ENTRYPOINT [ "sh", "-c", "liquibase $COMMAND $TARGET_LIQUIBASE_TAG --changelog-file=$CHANGELOG_FILE -Dschemaname=wfprev"]
ENTRYPOINT ["sh", "-c", "./executeLiquibase.sh"]