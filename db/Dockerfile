FROM liquibase/liquibase:5.0

RUN lpm add postgresql --global
COPY ./scripts ./scripts
COPY ./*changelog.json .
COPY ./liquibase.properties .
COPY --chmod=0755 ./executeLiquibase.sh .

# Old command replaced by command below to run two changelog files
#ENTRYPOINT [ "sh", "-c", "liquibase $COMMAND $TARGET_LIQUIBASE_TAG --changelog-file=$CHANGELOG_FILE -Dschemaname=wfprev"]
ENTRYPOINT ["sh", "-c", "./executeLiquibase.sh"]
