#!/bin/bash
export LIQUIBASE_COMMAND_USERNAME="wfprev"
export LIQUIBASE_COMMAND_PASSWORD="$WFPREV_LIQUIBASE_COMMAND_PASSWORD"
liquibase $COMMAND $TARGET_LIQUIBASE_TAG --username=wfprev --changelog-file=wfprev-changelog.json
export LIQUIBASE_COMMAND_USERNAME="app_wf1_prev"
export LIQUIBASE_COMMAND_PASSWORD="$APP_WF1_PREV_LIQUIBASE_COMMAND_PASSWORD"
liquibase $COMMAND $TARGET_LIQUIBASE_TAG --username=app_wf1_prev --changelog-file=app_wf1_prev-changelog.json