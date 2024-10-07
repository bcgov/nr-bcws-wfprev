variable "common_tags" {
  description = "Common tags for created resources"
  default = {
    Application = "WFPREV"
  }
}

variable "WFPREV_CLIENT_CPU_UNITS" {
  description = "client instance CPU units to provision (1 vCPU = 1024 CPU units)"
  type        = number
  default = 512
}

variable "WFPREV_CLIENT_MEMORY" {
  description = "client instance memory to provision (in MiB)"
  type        = number
  default = 1024
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

variable "server_port" {
  description = "Port exposed by the docker image to redirect traffic to"
  default     = 443
}

variable "LOGGING_LEVEL" {
  type        = string
  description = "Logging level for components"
}

variable "aws_region" {
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

variable "WFPREV_USERNAME" {
  type    = string
  default = ""
}

variable "DB_PASS" {
  description = "db password, passed in as env variable at runtime"
  type        = string
  default = ""
}

variable "api_key" {
  description = "value for api key"
  type        = string
  default = ""
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
  default = ""
}

variable "WFPREV_API_PORT" {
  description = "Port exposed by the docker image to redirect traffic to"
  type = number
  default  = 8080
}

variable "WFPREV_CLIENT_PORT" {
  type = number
  default = 8080
}

variable "WEBADE_OAUTH2_WFPREV_UI_CLIENT_SECRET" {
  type    = string
  default = ""
}

variable "WEBADE-OAUTH2_CHECK_TOKEN_URL" {
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
  type = string
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
  default = ""
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
  type = list(string)
  description = "List of paths to point at payroll API"
  default = ["wfprev", "wfprev/*"]
}

variable "PREVENTION_API_NAMES" {
  type = list(string)
  description = "List of paths to point at payroll API"
  default = ["wfprev-api", "wfprev-api/*"]
}
