# ecs.tf

resource "aws_ecs_cluster" "wfprev_main" {
  name = "wfprev-cluster"

  # tags = local.common_tags
}

resource "aws_ecs_cluster_capacity_providers" "wfprev_main_providers" {
  cluster_name = aws_ecs_cluster.wfprev_main.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    base              = 1
    weight            = 100
    capacity_provider = "FARGATE_SPOT"
  }
}

//////////////////////////////
////   TASK DEFINITIONS   ////
//////////////////////////////

# WFPrev Server Task Definition
resource "aws_ecs_task_definition" "wfprev_server" {
  family                   = "wfprev-server-task-${var.TARGET_ENV}"
  execution_role_arn       = aws_iam_role.wfprev_ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.wfprev_app_container_role.arn
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.WFPREV_API_CPU_UNITS
  memory                   = var.WFPREV_API_MEMORY
  volume {
    name = "work"
  }
  volume {
    name = "logging"
  }
  volume {
    name = "temp"
  }
  container_definitions = jsonencode([{
    essential              = true
    readonlyRootFilesystem = true
    name                   = var.server_container_name
    image                  = var.server_image
    repositoryCredentials = {
      credentialsParameter = aws_secretsmanager_secret.githubCredentials.arn
    }
    cpu                    = var.WFPREV_API_CPU_UNITS
    memory                 = var.WFPREV_API_MEMORY
    networkMode            = "awsvpc"
    portMappings           = [
      {
      protocol      = "tcp"
      containerPort = var.server_port
      hostPort      = var.server_port
      }
    ]
    environment = [
      {
        name  = "LOGGING_LEVEL"
        value = var.LOGGING_LEVEL
      },
      {
        name  = "AWS_REGION"
        value = var.aws_region
      },
      {
        name  = "WEBADE_OAUTH2_CLIENT_ID"
        value = var.WEBADE_OAUTH2_REST_CLIENT_ID
      },
      {
        name  = "WEBADE-OAUTH2_TOKEN_URL",
        value = var.WEBADE-OAUTH2_TOKEN_URL
      },
      {
        name = "WFPREV_DB_URL"
        value = "jdbc:postgresql://${aws_db_instance.wfprev_pgsqlDB.endpoint}/${aws_db_instance.wfprev_pgsqlDB.name}"
      },
      {
        name  = "WFPREV_USERNAME"
        value = var.WFPREV_USERNAME
      },
      {
        name  = "DB_PASS"
        value = var.DB_PASS
      },
      {
        name  = "API_KEY"
        value = var.api_key
      }
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        awslogs-create-group  = "true"
        awslogs-group         = "/ecs/${var.server_name}"
        awslogs-region        = var.aws_region
        awslogs-stream-prefix = "ecs"
      }
    }
    mountPoints = [
      {
        sourceVolume = "logging"
        containerPath = "/usr/local/tomcat/logs"
        readOnly = false
      },
      {
        sourceVolume = "work"
        containerPath = "/usr/local/tomcat/work"
        readOnly = false
      },
      {
        sourceVolume = "temp"
        containerPath = "/usr/local/tomcat/temp"
        readOnly = false
      }
    ]
    volumesFrom = []
  }])
}

# WFPrev Client Task Definition

resource "aws_ecs_task_definition" "wfprev_client" {
  family                   = "wfprev-client-task-${var.TARGET_ENV}"
  execution_role_arn       = aws_iam_role.wfprev_ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.wfprev_app_container_role.arn
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.WFPREV_CLIENT_CPU_UNITS
  memory                   = var.WFPREV_CLIENT_MEMORY
  volume {
    name = "work"
  }
  volume {
    name = "logging"
  }
  container_definitions = jsonencode([
    {
      essential   = true
      readonlyRootFilesystem = true
      name        = var.client_container_name
      image       = var.CLIENT_IMAGE
      cpu         = var.WFPREV_CLIENT_CPU_UNITS
      memory      = var.WFPREV_CLIENT_MEMORY
      networkMode = "awsvpc"
      portMappings = [
        {
          protocol      = "tcp"
          containerPort = var.client_port
          hostPort      = var.client_port
        }
      ]
      environment = [
        {
          name  = "LOGGING_LEVEL"
          value = "${var.LOGGING_LEVEL}"
        },
        {
          name  = "AWS_REGION",
          value = var.aws_region
        },
        {
          #Base URL will use the 
          name  = "BASE_URL",
          value = var.TARGET_ENV == "prod" ? "https://${var.gov_client_url}/" : "${aws_apigatewayv2_stage.wfprev_stage.invoke_url}/wfprev-ui"
        },
        {
          name  = "WEBADE_OAUTH2_WFPREV_REST_CLIENT_SECRET",
          value = var.WEBADE_OAUTH2_WFPREV_UI_CLIENT_SECRET
        },
        {
          name  = "WEBADE-OAUTH2_TOKEN_URL",
          value = var.WEBADE-OAUTH2_TOKEN_URL
        },
        {
          name  = "WEBADE-OAUTH2_CHECK_TOKEN_V2_URL"
          value = var.WEBADE-OAUTH2_CHECK_TOKEN_URL
        },
        { //Will be phased out from prod eventually, but not yet  "https://${aws_route53_record.wfprev_nginx.name}/"
          name  = "WFPREV_API_URL",
          value = var.TARGET_ENV == "prod" ? "https://${var.gov_api_url}/" : "https://example.com/"
        },
        {
          name  = "APPLICATION_ENVIRONMENT",
          value = var.TARGET_ENV != "prod" ? var.TARGET_ENV : " "
        },
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-create-group  = "true"
          awslogs-group         = "/ecs/${var.client_name}"
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
      mountPoints = [
        {
          sourceVolume = "logging"
          containerPath = "/usr/local/tomcat/logs"
          readOnly = false
        },
        {
          sourceVolume = "work"
          containerPath = "/usr/local/tomcat/work"
          readOnly = false
        }
        ]
      volumesFrom = []
    }
  ])
}


# Placeholder for Other Task Definitions like Nginx, Liquibase, etc.
# For each additional component, create similar task definitions based on wfprev structure

//////////////////////////////
////   SERVICES   ////
//////////////////////////////

# ECS Service for WFPrev Server
resource "aws_ecs_service" "wfprev_server" {
  name                              = "wfprev-server-service-${var.TARGET_ENV}"
  cluster                           = aws_ecs_cluster.wfprev_main.id
  task_definition                   = aws_ecs_task_definition.wfprev_server.arn
  desired_count                     = var.server_count
  enable_ecs_managed_tags           = true
  propagate_tags                    = "TASK_DEFINITION"
  health_check_grace_period_seconds = 60
  wait_for_steady_state             = false

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 80
  }
  capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 20
    base              = 1
  }

  network_configuration {
    security_groups  = [data.aws_security_group.app.id]
    subnets          = module.network.aws_subnet_ids.app.ids
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.wfprev_api.id
    container_name   = var.server_container_name
    container_port   = var.server_port
  }

  # depends_on = [aws_iam_role_policy_attachment.wfprev_ecs_task_execution_role]

  # tags = local.common_tags
}

# ECS Service for WFPrev Client

resource "aws_ecs_service" "client" {
  name                              = "wfprev-client-service-${var.TARGET_ENV}"
  cluster                           = aws_ecs_cluster.wfprev_main.id
  task_definition                   = aws_ecs_task_definition.wfprev_client.arn
  desired_count                     = var.APP_COUNT
  enable_ecs_managed_tags           = true
  propagate_tags                    = "TASK_DEFINITION"
  health_check_grace_period_seconds = 60
  wait_for_steady_state             = false


  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 80
  }
  capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 20
    base              = 1
  }


  network_configuration {
    security_groups  = [data.aws_security_group.app.id]
    subnets          = module.network.aws_subnet_ids.app.ids
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.wfprev_ui.id
    container_name   = var.client_container_name
    container_port   = var.client_port
  }

  # depends_on = [aws_iam_role_policy_attachment.wfprev_ecs_task_execution_role]
}


# Placeholder for other ECS Services like Nginx, Liquibase, etc.
# Define similar ECS services for additional task definitions.
