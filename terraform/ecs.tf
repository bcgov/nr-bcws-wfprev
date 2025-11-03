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
  family                   = "wfprev-server-task-${var.SHORTENED_ENV}"
  execution_role_arn       = aws_iam_role.wfprev_ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.wfprev_app_container_role.arn
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.WFPREV_API_CPU_UNITS
  memory                   = var.WFPREV_API_MEMORY
  volume {
    name = "temp"
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
        name  = "WFPREV_CLIENT_ID"
        value = var.WFPREV_CLIENT_ID
      },
      {
        name  = "WFPREV_CLIENT_SECRET",
        value = var.WFPREV_CLIENT_SECRET
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
        name  = "WFPREV_BASE_URL",
        value = var.WFPREV_BASE_URL
      },
      {
        name  = "WFPREV_GDB_FUNCTION_NAME",
        value = var.WFPREV_GDB_FUNCTION_NAME
      }, 
      {
        name  = "WFDM_BASE_URL",
        value = var.WFDM_BASE_URL
      },
      {
        name  = "GEOSERVER_API_BASE_URL",
        value = var.GEOSERVER_API_BASE_URL
      },
      {
        name  = "WFNEWS_API_BASE_URL",
        value = var.WFNEWS_API_BASE_URL
      },
       {
        name  = "WFNEWS_API_KEY",
        value = var.WFNEWS_API_KEY
      },
      {
        name  = "OPENMAPS_URL",
        value = var.OPENMAPS_URL
      },
      {
        name  = "WFPREV_CHECK_TOKEN_URL",
        value = var.WFPREV_CHECK_TOKEN_URL
      },
      {
        name = "WFPREV_DATASOURCE_URL"
        value = "jdbc:postgresql://${aws_db_instance.wfprev_pgsqlDB.endpoint}/${aws_db_instance.wfprev_pgsqlDB.db_name}"
      },
      {
        name  = "WFPREV_DATASOURCE_USERNAME"
        value = var.PROXY_WF1_PREV_REST_USER
      },
      {
        name  = "WFPREV_DATASOURCE_PASSWORD"
        value = var.PROXY_WF1_PREV_REST_PASSWORD
      },
      {
        name  = "API_KEY"
        value = var.api_key
      },
      {
        name = "REPORT_GENERATOR_LAMBDA_URL"
        value = aws_lambda_function_url.report_generator_url.function_url
      },
      {
        name  = "TRAINING_AND_SUPPORT_LINK"
        value = var.TRAINING_AND_SUPPORT_LINK
      },
      {
        name  = "REMI_PLANNER_EMAIL_ADDRESS"
        value = var.REMI_PLANNER_EMAIL_ADDRESS
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
        sourceVolume = "temp"
        containerPath = "/tmp"
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
  family = "wfprev-liquibase-${var.SHORTENED_ENV}"
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
          awslogs-group         = "/ecs/wfprev-liquibase-nonproxy-${var.SHORTENED_ENV}"
          awslogs-region        = var.AWS_REGION
          awslogs-stream-prefix = "ecs"
        }
      }
      environment = [
        {
          name = "LIQUIBASE_COMMAND_URL"
          value = "jdbc:postgresql://${aws_db_instance.wfprev_pgsqlDB.endpoint}/${aws_db_instance.wfprev_pgsqlDB.db_name}"
        },
        {
          name = "LIQUIBASE_COMMAND_USERNAME"
          value = var.WFPREV_USERNAME
        },
        {
          name = "LIQUIBASE_COMMAND_PASSWORD"
          value = var.DB_PASS
        },
        {
          name = "APP_WF1_PREV_PASSWORD"
          value = var.APP_WF1_PREV_PASSWORD
        },
        {
          name = "PROXY_WF1_PREV_REST_PASSWORD"
          value = var.PROXY_WF1_PREV_REST_PASSWORD
        },
        {
          name = "WFPREV_DATABASE_NAME"
          value = var.WFPREV_DATABASE_NAME
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
      --task-definition wfprev-liquibase-${var.SHORTENED_ENV} \
      --cluster ${aws_ecs_cluster.wfprev_main.id} \
      --count 1 \
      --network-configuration awsvpcConfiguration={securityGroups=[${module.networking.security_groups.app.id}],subnets=${module.networking.subnets.app.ids[0]},assignPublicIp=DISABLED}
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
  name                              = "wfprev-server-service-${var.SHORTENED_ENV}"
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
    security_groups  = [module.networking.security_groups.app.id, aws_security_group.wfprev_tomcat_access.id]
    subnets          = module.networking.subnets.app.ids
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.wfprev_api.id
    container_name   = var.server_container_name
    container_port   = var.WFPREV_API_PORT
  }
}
