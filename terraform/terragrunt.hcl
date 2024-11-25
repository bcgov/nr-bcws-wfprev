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
    bucket         = "terraform-remote-state-${get_env("TFC_PROJECT")}-${get_env("TARGET_ENV")}"    # Replace with either generated or custom bucket name
    key            = "terraform.${get_env("TFC_PROJECT")}-${get_env("TARGET_ENV")}-state"           # Path and name of the state file within the bucket
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
APP_COUNT = "${get_env("APP_COUNT")}"
LOGGING_LEVEL = "${get_env("LOGGING_LEVEL")}"

# server
WFPREV_API_NAME = "${get_env("WFPREV_API_NAME")}"
WFPREV_API_IMAGE = "${get_env("WFPREV_API_IMAGE")}"
WFPREV_API_CPU_UNITS = "${get_env("WFPREV_API_CPU_UNITS")}"
WFPREV_API_MEMORY = "${get_env("WFPREV_API_MEMORY")}"
WFPREV_API_PORT = "${get_env("WFPREV_API_PORT")}"
WEBADE_OAUTH2_CHECK_TOKEN_URL = "${get_env("WEBADE_OAUTH2_CHECK_TOKEN_URL")}"
WEBADE_OAUTH2_CHECK_AUTHORIZE_URL = "${get_env("WEBADE_OAUTH2_CHECK_AUTHORIZE_URL")}"
WFPREV_DATASOURCE_URL = "${get_env("WFPREV_DATASOURCE_URL")}"
WFPREV_DATASOURCE_USERNAME = "${get_env("WFPREV_DATASOURCE_USERNAME")}"
WFPREV_DATASOURCE_PASSWORD = "${get_env("WFPREV_DATASOURCE_PASSWORD")}"
server_count = "${get_env("server_count")}"

TARGET_AWS_ACCOUNT_ID = "${get_env("TARGET_AWS_ACCOUNT_ID")}"

# client
WEBADE_OAUTH2_WFPREV_UI_CLIENT_SECRET = "${get_env("WEBADE_OAUTH2_WFPREV_UI_CLIENT_SECRET")}"
CLIENT_IMAGE = "${get_env("CLIENT_IMAGE")}"

# db
WFPREV_USERNAME = "${get_env("WFPREV_USERNAME")}"
DB_PASS = "${get_env("DB_PASS")}"
DB_INSTANCE_TYPE = "${get_env("DB_INSTANCE_TYPE")}"

# liquibase
COMMAND = "${get_env("COMMAND")}"
CHANGELOG_NAME = "${get_env("CHANGELOG_NAME")}"
LIQUIBASE_IMAGE = "${get_env("LIQUIBASE_IMAGE")}"
LIQUIBASE_COMMAND_USERNAME = "${get_env("LIQUIBASE_COMMAND_USERNAME")}"
LIQUIBASE_COMMAND_PASSWORD = "${get_env("LIQUIBASE_COMMAND_PASSWORD")}"
SCHEMA_NAME = "${get_env("SCHEMA_NAME")}"
TARGET_LIQUIBASE_TAG = "${get_env("TARGET_LIQUIBASE_TAG")}"
PROXY_COUNT="${get_env("PROXY_COUNT")}"
NONPROXY_COUNT="${get_env("NONPROXY_COUNT")}"

EOF
}

generate "provider" {
  path      = "provider.tf"
  if_exists = "overwrite"
  contents  = <<EOF
provider "aws" {
  region  = "ca-central-1"
  assume_role {
    role_arn = "arn:aws:iam::$${var.TARGET_AWS_ACCOUNT_ID}:role/Terraform-deploy"
  }
}
EOF
}
