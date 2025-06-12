#!/bin/bash
export LIQUIBASE_COMMAND_USERNAME="$LIQUIBASE_COMMAND_USERNAME"
export LIQUIBASE_COMMAND_PASSWORD="$LIQUIBASE_COMMAND_PASSWORD"
liquibase $COMMAND $TARGET_LIQUIBASE_TAG --username=wfprev --changelog-file=wfprev-changelog.json
export LIQUIBASE_COMMAND_USERNAME="app_wf1_prev"
export LIQUIBASE_COMMAND_PASSWORD="$APP_WF1_PREV_PASSWORD"
liquibase $COMMAND $TARGET_LIQUIBASE_TAG --username=app_wf1_prev --changelog-file=app_wf1_prev-changelog.json