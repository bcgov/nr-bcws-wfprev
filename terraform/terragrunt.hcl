locals {
  tfc_hostname     = "app.terraform.io"
  tfc_organization = "bcgov"
}

generate "remote_state" {
  path      = "backend.tf"
  if_exists = "overwrite"
  contents  = <<EOF
terraform {
  backend "s3" {
    bucket         = "terraform-remote-state-${get_env("TFC_PROJECT")}-${get_env("NAMESPACE_ENV")}"    # Replace with either generated or custom bucket name
    key            = "terraform.${get_env("TFC_PROJECT")}-${get_env("NAMESPACE_ENV")}-state"           # Path and name of the state file within the bucket
    region         = "ca-central-1"                   # AWS region where the bucket is located
    dynamodb_table = "terraform-remote-state-lock-${get_env("TFC_PROJECT")}"  # Replace with either generated or custom DynamoDB table name
    encrypt        = true                              # Enable encryption for the state file
  }
}
EOF
}

generate "tfvars" {
  path              = "terragrunt.auto.tfvars"
  if_exists         = "overwrite"
  disable_signature = true
  contents          = <<-EOF
TARGET_ENV = "${get_env("TARGET_ENV")}"
NAMESPACE_ENV = "${get_env("NAMESPACE_ENV")}"
SHORTENED_ENV = "${get_env("SHORTENED_ENV")}"
TFC_PROJECT = "${get_env("TFC_PROJECT")}"
APP_COUNT = "${get_env("APP_COUNT")}"
LOGGING_LEVEL = "${get_env("LOGGING_LEVEL")}"
RESTORE_DOWNSCALED_CLUSTER = "${get_env("RESTORE_DOWNSCALED_CLUSTER")}"

# server
WFPREV_API_NAME = "${get_env("WFPREV_API_NAME")}"
WFPREV_API_IMAGE = "${get_env("WFPREV_API_IMAGE")}"
WFPREV_API_CPU_UNITS = "${get_env("WFPREV_API_CPU_UNITS")}"
WFPREV_API_MEMORY = "${get_env("WFPREV_API_MEMORY")}"
WFPREV_MAX_SCALING_CAPACITY = "${get_env("WFPREV_MAX_SCALING_CAPACITY")}"
WFPREV_API_PORT = "${get_env("WFPREV_API_PORT")}"
WFPREV_CLIENT_ID = "${get_env("WFPREV_CLIENT_ID")}"
WFPREV_CLIENT_SECRET = "${get_env("WFPREV_CLIENT_SECRET")}"
WEBADE_OAUTH2_CHECK_TOKEN_URL = "${get_env("WEBADE_OAUTH2_CHECK_TOKEN_URL")}"
WEBADE_OAUTH2_CHECK_AUTHORIZE_URL = "${get_env("WEBADE_OAUTH2_CHECK_AUTHORIZE_URL")}"
WFPREV_BASE_URL = "${get_env("WFPREV_BASE_URL")}"
WFDM_BASE_URL = "${get_env("WFDM_BASE_URL")}"
WFPREV_GDB_FUNCTION_NAME = "${get_env("WFPREV_GDB_FUNCTION_NAME")}"
OPENMAPS_URL = "${get_env("OPENMAPS_URL")}"
server_count = "${get_env("server_count")}"

# AWS
TARGET_AWS_ACCOUNT_ID = "${get_env("TARGET_AWS_ACCOUNT_ID")}"
AWS_ALERT_EMAIL_LIST = "${get_env("AWS_ALERT_EMAIL_LIST")}"

# client
WEBADE_OAUTH2_WFPREV_UI_CLIENT_SECRET = "${get_env("WEBADE_OAUTH2_WFPREV_UI_CLIENT_SECRET")}"
CLIENT_IMAGE = "${get_env("CLIENT_IMAGE")}"
WFPREV_CHECK_TOKEN_URL = "${get_env("WFPREV_CHECK_TOKEN_URL")}"
TRAINING_AND_SUPPORT_LINK = "${get_env("TRAINING_AND_SUPPORT_LINK")}"
REMI_PLANNER_EMAIL_ADDRESS = "${get_env("REMI_PLANNER_EMAIL_ADDRESS")}"
GEOSERVER_API_BASE_URL = "${get_env("GEOSERVER_API_BASE_URL")}"
WFNEWS_API_BASE_URL = "${get_env("WFNEWS_API_BASE_URL")}"
WFNEWS_API_KEY = "${get_env("WFNEWS_API_KEY")}"

# node
WFPREV_GDB_EXTRACTOR_IMAGE = "${get_env("WFPREV_GDB_EXTRACTOR_IMAGE")}"

# db
WFPREV_USERNAME = "${get_env("WFPREV_USERNAME")}"
DB_PASS = "${get_env("DB_PASS")}"
DB_INSTANCE_TYPE = "${get_env("DB_INSTANCE_TYPE")}"

# liquibase
COMMAND = "${get_env("COMMAND")}"
LIQUIBASE_IMAGE = "${get_env("LIQUIBASE_IMAGE")}"
PROXY_WF1_PREV_REST_PASSWORD = "${get_env("PROXY_WF1_PREV_REST_PASSWORD")}"
APP_WF1_PREV_PASSWORD = "${get_env("APP_WF1_PREV_PASSWORD")}"
WFPREV_DATABASE_NAME = "${get_env("WFPREV_DATABASE_NAME")}"
TARGET_LIQUIBASE_TAG = "${get_env("TARGET_LIQUIBASE_TAG")}"
PROXY_COUNT="${get_env("PROXY_COUNT")}"
NONPROXY_COUNT="${get_env("NONPROXY_COUNT")}"

# report generator
WFPREV_REPORT_GENERATOR_IMAGE = "${get_env("WFPREV_REPORT_GENERATOR_IMAGE")}"

EOF
}

generate "provider" {
  path      = "provider.tf"
  if_exists = "overwrite"
  contents  = <<EOF
provider "aws" {
  region  = "ca-central-1"
  assume_role {
    role_arn = "arn:aws:iam::$${var.TARGET_AWS_ACCOUNT_ID}:role/GHA_CI_CD"
  }
}

provider "aws" {
  alias = "aws-us"
  region  = "us-east-1"
  assume_role {
    role_arn = "arn:aws:iam::$${var.TARGET_AWS_ACCOUNT_ID}:role/GHA_CI_CD"
  }
}
EOF
}
