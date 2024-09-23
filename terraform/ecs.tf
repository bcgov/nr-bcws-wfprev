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
  family                   = "wfprev-server-task-${var.target_env}"
  execution_role_arn       = aws_iam_role.wfprev_ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.wfprev_app_container_role.arn
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.server_cpu_units
  memory                   = var.server_memory
  volume {
    name = "work"
  }
  volume {
    name = "logging"
  }
  volume {
    name = "temp"
  }
  tags                     = local.common_tags
  container_definitions = jsonencode([{
    essential              = true
    readonlyRootFilesystem = true
    name                   = var.server_container_name
    image                  = var.server_image
    cpu                    = var.server_cpu_units
    memory                 = var.server_memory
    networkMode            = "awsvpc"
    portMappings           = [{
      protocol      = "tcp"
      containerPort = var.server_port
      hostPort      = var.server_port
    }]
    environment = [
      {
        name  = "LOGGING_LEVEL"
        value = var.logging_level
      },
      {
        name  = "DB_NAME"
        value = aws_db_instance.wfprev_pgsqlDB.name
      },
      {
        name  = "AWS_REGION"
        value = var.aws_region
      },
      {
        name  = "bucketName"
        value = aws_s3_bucket.wfprev_upload_bucket.id
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
        name = "DEFAULT_APPLICATION_ENVIRONMENT"
        value = var.DEFAULT_APPLICATION_ENVIRONMENT
      },
      {
        name = "WFPREV_DB_URL"
        value = "jdbc:postgresql://${aws_db_instance.wfprev_pgsqlDB.endpoint}/${aws_db_instance.wfprev_pgsqlDB.name}"
      },
      {
        name  = "WFNEWS_USERNAME"
        value = var.WFPREV_USERNAME
      },
      {
        name  = "DB_PASS"
        value = var.db_pass
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

# Placeholder for Other Task Definitions like Nginx, Liquibase, etc.
# For each additional component, create similar task definitions based on wfnews structure

//////////////////////////////
////   SERVICES   ////
//////////////////////////////

# ECS Service for WFPrev Server
resource "aws_ecs_service" "wfprev_server" {
  name                              = "wfprev-server-service-${var.target_env}"
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
    security_groups  = [aws_security_group.wfprev_ecs_tasks.id, data.aws_security_group.app.id]
    subnets          = module.network.aws_subnet_ids.app.ids
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.wfprev_server.id
    container_name   = var.server_container_name
    container_port   = var.server_port
  }

  # depends_on = [aws_iam_role_policy_attachment.wfprev_ecs_task_execution_role]

  # tags = local.common_tags
}

# Placeholder for other ECS Services like Nginx, Liquibase, etc.
# Define similar ECS services for additional task definitions.
