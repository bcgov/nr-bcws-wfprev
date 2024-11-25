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
  volume {
    name = "webapps"
  }
  container_definitions = jsonencode([{
    essential              = true
    readonlyRootFilesystem = true
    name                   = var.server_container_name
    image                  = var.WFPREV_API_IMAGE
    cpu                    = var.WFPREV_API_CPU_UNITS
    memory                 = var.WFPREV_API_MEMORY
    networkMode            = "awsvpc"
    portMappings           = [
      {
      protocol      = "tcp"
      containerPort = var.WFPREV_API_PORT
      hostPort      = var.WFPREV_API_PORT
      }
    ]
    environment = [
      {
        name  = "LOGGING_LEVEL"
        value = var.LOGGING_LEVEL
      },
      {
        name  = "AWS_REGION"
        value = var.AWS_REGION
      },
      {
        name  = "WEBADE_OAUTH2_CHECK_TOKEN_URL"
        value = var.WEBADE_OAUTH2_CHECK_TOKEN_URL
      },
      {
        name  = "WEBADE_OAUTH2_CHECK_AUTHORIZE_URL",
        value = var.WEBADE_OAUTH2_CHECK_AUTHORIZE_URL
      },
      {
        name = "WFPREV_DATASOURCE_URL"
        value = "jdbc:postgresql://${aws_db_instance.wfprev_pgsqlDB.endpoint}/${aws_db_instance.wfprev_pgsqlDB.name}"
      },
      {
        name  = "WFPREV_DATASOURCE_USERNAME"
        value = var.WFPREV_DATASOURCE_USERNAME
      },
      {
        name  = "WFPREV_DATASOURCE_PASSWORD"
        value = var.WFPREV_DATASOURCE_PASSWORD
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
        awslogs-region        = var.AWS_REGION
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
      },
      {
        sourceVolume = "webapps"
        containerPath = "/usr/local/tomcat/webapps"
        readOnly = false
      }
    ]
    volumesFrom = []
  }])
}

# WFPrev Liquibase Task Definition

resource "null_resource" "always_run" {
  triggers = {
    timestamp = "${timestamp()}"
  }
}

resource "aws_ecs_task_definition" "wfprev-liquibase" {
  count = var.NONPROXY_COUNT
  family = "wfprev-liquibase-${var.TARGET_ENV}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu = 256
  memory = 512
  execution_role_arn = aws_iam_role.wfprev_ecs_task_execution_role.arn
  container_definitions = jsonencode([{
      essential   = true
      name        = "wfprev-liquibase"
      image       = var.LIQUIBASE_IMAGE
      cpu         = 256
      memory      = 512
      portMappings = [
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-create-group  = "true"
          awslogs-group         = "/ecs/wfprev-liquibase-nonproxy-${var.TARGET_ENV}"
          awslogs-region        = var.AWS_REGION
          awslogs-stream-prefix = "ecs"
        }
      }
      environment = [
        {
          name = "LIQUIBASE_COMMAND_URL"
          value = "jdbc:postgresql://${aws_db_instance.wfprev_pgsqlDB.endpoint}/${aws_db_instance.wfprev_pgsqlDB.name}"
        },
        {
          name = "CHANGELOG_FILE"
          value = "${var.CHANGELOG_NAME}.json"
        },
        {
          name = "LIQUIBASE_COMMAND_USERNAME"
          value = var.LIQUIBASE_COMMAND_USERNAME
        },
        {
          name = "LIQUIBASE_COMMAND_PASSWORD"
          value = var.LIQUIBASE_COMMAND_PASSWORD
        },
        {
          name = "SCHEMA_NAME"
          value = var.SCHEMA_NAME
        },
        {
          name = "TARGET_LIQUIBASE_TAG"
          value = var.TARGET_LIQUIBASE_TAG
        },
        {
          name = "COMMAND"
          value = var.COMMAND
        }

      ]
  }])

  lifecycle {
    replace_triggered_by = [
      null_resource.always_run
    ]
  }
  provisioner "local-exec" {
    command = <<-EOF
    aws ecs run-task \
      --task-definition wfprev-liquibase-${var.TARGET_ENV} \
      --cluster ${aws_ecs_cluster.wfprev_main.id} \
      --count 1 \
      --network-configuration awsvpcConfiguration={securityGroups=[${data.aws_security_group.app.id}],subnets=${module.network.aws_subnet_ids.app.ids[0]},assignPublicIp=DISABLED}
EOF
  }
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
    security_groups  = [data.aws_security_group.app.id, aws_security_group.wfprev_tomcat_access.id]
    subnets          = module.network.aws_subnet_ids.app.ids
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.wfprev_api.id
    container_name   = var.server_container_name
    container_port   = var.WFPREV_API_PORT
  }

  # depends_on = [aws_iam_role_policy_attachment.wfprev_ecs_task_execution_role]

  # tags = local.common_tags
}

# Placeholder for other ECS Services like Nginx, Liquibase, etc.
# Define similar ECS services for additional task definitions.
