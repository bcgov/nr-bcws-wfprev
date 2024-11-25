variable "common_tags" {
  description = "Common tags for created resources"
  default = {
    Application = "WFPREV"
  }
}

variable "WFPREV_API_CPU_UNITS" {
  description = "server CPU units to provision (1 vCPU = 1024 CPU units)"
  type        = number
}

variable "WFPREV_API_MEMORY" {
  description = "server memory to provision (in MiB)"
  type        = number
}

variable "client_container_name" {
  description = "Container name"
  default     = "wfprev-client-app"
}

variable "server_container_name" {
  description = "Server container name"
  default     = "wfprev-server-app"
}

variable "CLIENT_IMAGE" {
  description = "Docker image to run in the ECS cluster. _Note_: there is a blank default value, which will cause service and task resource creation to be supressed unless an image is specified."
  type        = string
  default     = ""
}
variable "SERVER_IMAGE" {
  description = "Docker image to run in the ECS cluster. _Note_: there is a blank default value, which will cause service and task resource creation to be supressed unless an image is specified."
  type        = string
  default     = ""
}

variable "WFPREV_API_IMAGE" {
  description = "Docker image to run in the ECS cluster. _Note_: there is a blank default value, which will cause service and task resource creation to be supressed unless an image is specified."
  type        = string
  default     = ""
}

variable "LOGGING_LEVEL" {
  type        = string
  description = "Logging level for components"
}

variable "AWS_REGION" {
  description = "The AWS region things are created in"
  default     = "ca-central-1"
}

variable "WEBADE_OAUTH2_REST_CLIENT_ID" {
  type    = string
  default = ""
}

variable "WEBADE-OAUTH2_TOKEN_CLIENT_URL" {
  type    = string
  default = ""
}

variable "WEBADE-OAUTH2_TOKEN_URL" {
  type    = string
  default = ""
}

variable "WEBADE_OAUTH2_CHECK_TOKEN_URL" {
  type    = string
  default = ""
}

variable "WEBADE_OAUTH2_CHECK_AUTHORIZE_URL" {
  type    = string
  default = ""
}

variable "WFPREV_DATASOURCE_USERNAME" {
  type    = string
  default = ""
}

variable "WFPREV_DATASOURCE_PASSWORD" {
  description = "db password, passed in as env variable at runtime"
  type        = string
  default     = ""
}

variable "WFPREV_USERNAME" {
  type    = string
  default = ""
}

variable "DB_PASS" {
  description = "db password, passed in as env variable at runtime"
  type        = string
  default     = ""
}

variable "api_key" {
  description = "value for api key"
  type        = string
  default     = ""
}

variable "server_name" {
  description = "Name of the server"
  type        = string
  default     = "wfprev-server-app"
}

variable "server_count" {
  description = "Number of docker containers to run"
  default     = 2
}

//load balancers
variable "ALB_NAME" {
  description = "Name of the internal alb"
  default     = "default"
  type        = string
}

variable "TARGET_ENV" {
  description = "AWS workload account env (e.g. dev, test, prod, sandbox, unclass)"
  type        = string
  default     = ""
}

variable "WFPREV_API_PORT" {
  description = "Port exposed by the docker image to redirect traffic to"
  type        = number
  default     = 8080
}

variable "WEBADE_OAUTH2_WFPREV_UI_CLIENT_SECRET" {
  type    = string
  default = ""
}

variable "client_name" {
  description = "Name of the client"
  type        = string
  default     = "wfprev-client-app"
}

variable "gov_client_url" {
  description = "domain name if using *.nrs.gov.bc.ca url"
  default     = ""
  type        = string
}

variable "APP_COUNT" {
  description = "Number of docker containers to run"
  default     = 2
}

variable "ecs_task_execution_role_name" {
  description = "ECS task execution role name"
  default     = "wfprevEcsTaskExecutionRole"
}

variable "gov_api_url" {
  description = "domain name if using *-api.nrs.gov.bc.ca url"
  default     = ""
  type        = string
}

variable "TARGET_AWS_ACCOUNT_ID" {
  type        = string
  description = "Numerical AWS account ID"
}

variable "DB_POSTGRES_VERSION" {
  description = "Which version of Postgres to use"
  default     = "15.4"
  type        = string
}

variable "DB_INSTANCE_TYPE" {
  description = "Instance type to use for database vm"
  type        = string
  default     = ""
}

variable "DB_MULTI_AZ" {
  description = "Whether to make db deployment a multi-AZ deployment"
  default     = false
  type        = bool
}

variable "DB_SIZE" {
  description = "size of db, in GB"
  type        = number
  default     = 10
}


variable "PREVENTION_WAR_NAMES" {
  type        = list(string)
  description = "List of paths to point at payroll API"
  default     = ["wfprev", "wfprev/*"]
}

variable "PREVENTION_API_NAMES" {
  type        = list(string)
  description = "List of paths to point at payroll API"
  default     = ["wfprev-api", "wfprev-api/*"]
}

//liquibase

variable "LIQUIBASE_MEMORY" {
  description = "Amount of memory to allocate to liquibase instances, in MB"
  type        = number
  default     = 512
}

variable "LIQUIBASE_CONTAINER_NAME" {
  description = "Name of DB container"
  default     = "wfprev-liquibase-app"
  type        = string
}

variable "LIQUIBASE_IMAGE" {
  description = "Full name of liquibase image"
  type        = string
  default     = ""
}

variable "LIQUIBASE_CPU" {
  description = "number of milliCPUs to allocate to liquibase instances"
  type        = number
  default     = 256
}

variable "DB_PORT" {
  description = "Port used to communicate with database"
  type        = number
  default     = 8080
}

variable "HEALTH_CHECK_PATH" {
  default = "/"
}

variable "LIQUIBASE_NAME" {
  description = "List of service names to use as subdomains"
  default     = ["wfnews-liquibase"]
  type        = list(string)
}

variable "CLOUDFRONT_HEADER" {
  description = "Header added when passing through cloudfront"
  default     = ""
  type        = string
}

variable "NONPROXY_COUNT" {
  type    = number
  default = 1
}

variable "CHANGELOG_NAME" {
  type    = string
  default = "main-changelog"
}

variable "LIQUIBASE_COMMAND_USERNAME" {
  type = string
}

variable "LIQUIBASE_COMMAND_PASSWORD" {
  type = string
}

variable "SCHEMA_NAME" {
  type = string
}

variable "TARGET_LIQUIBASE_TAG" {
  type = string
}

variable "COMMAND" {
  type    = string
  default = "update"
}

# Used if Terraform is uploading data externally into the Site s3 bucket
variable "MIME_TYPES" {
  description = "Map of file extensions to MIME types"
  type        = map(string)
  default = {
    ".html" = "text/html"
    ".css"  = "text/css"
    ".png"  = "image/png"
    ".jpg"  = "image/jpeg"
    ".jpeg" = "image/jpeg"
    ".pdf"  = "application/pdf"
    ".json" = "application/json"
    ".js"   = "application/javascript"
    ".gif"  = "image/gif"
    ".svg"  = "image/svg+xml"
    ".ico"  = "image/x-icon"
    ".txt"  = "text/plain"
    ".xml"  = "application/xml"
    ".mp4"  = "video/mp4"
    ".webm" = "video/webm"
    ".webp" = "image/webp"
    ".zip"  = "application/zip"
    ".doc"  = "application/msword"
    ".docx" = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    ".xls"  = "application/vnd.ms-excel"
    ".xlsx" = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ".csv"  = "text/csv"
    ".ttf"  = "font/ttf"
    ".md"   = "text/markdown"
    ".yaml" = "application/yaml"
    ".yml"  = "application/yaml"
  }
}
