resource "aws_db_instance" "wfprev_pgsqlDB" {
  identifier                      = "wfprev${var.SHORTENED_ENV}"
  engine                          = "postgres"
  engine_version                  = var.DB_POSTGRES_VERSION
  auto_minor_version_upgrade      = false
  allow_major_version_upgrade     = true
  db_name                         = "wfprev${var.SHORTENED_ENV}"
  instance_class                  = var.DB_INSTANCE_TYPE
  multi_az                        = var.DB_MULTI_AZ
  backup_retention_period         = 7
  allocated_storage               = var.DB_SIZE
  username                        = var.WFPREV_USERNAME
  password                        = var.DB_PASS
  db_subnet_group_name = aws_db_subnet_group.wfprev_db_subnet.name
  publicly_accessible             = false
  skip_final_snapshot             = true
  storage_encrypted               = true
  vpc_security_group_ids          = [module.networking.security_groups.data.id]
  snapshot_identifier             = var.RESTORE_DOWNSCALED_CLUSTER ? "wfprev${var.TARGET_ENV}-scaling-snapshot" : null
  enabled_cloudwatch_logs_exports = ["postgresql"]
  lifecycle {
    prevent_destroy = false
  }
}

resource "aws_db_subnet_group" "wfprev_db_subnet" {
  name       = "main"
  subnet_ids = module.networking.subnets.data.ids
}

/*
resource "aws_db_instance" "wfone_pgsqlDB" {
  identifier                      = "wfone${var.target_env}"
  engine                          = "postgres"
  engine_version                  = "13.4"
  auto_minor_version_upgrade      = false
  db_name                         = "wfone${var.target_env}"
  instance_class                  = var.db_instance_type
  multi_az                        = var.db_multi_az
  backup_retention_period         = 7
  allocated_storage               = var.db_size
  username                        = var.WFONE_USERNAME
  password                        = var.WFONE_DB_PASS
  publicly_accessible             = false
  skip_final_snapshot             = true
  storage_encrypted               = true
  vpc_security_group_ids          = [module.networking.security_groups.app.id, aws_security_group.wfone_ecs_tasks.id]
  tags                            = local.common_tags
  db_subnet_group_name            = aws_db_subnet_group.wfone_db_subnet_group.name
  enabled_cloudwatch_logs_exports = ["postgresql"]
  parameter_group_name            = "wfone-manual"
}
*/
/*
resource "aws_db_parameter_group" "wfnews_params" {
  name   = "wfnews-${var.target_env}"
  family = "postgres13"

  parameter {
    name  = "max_connections"
    value = "LEAST({DBInstanceClassMemory/2382848},5000)"
  }
}
*/
/*
resource "aws_db_proxy" "wfnews_db_proxy" {
  name                   = "wfnews-db-proxy-${var.target_env}"
  debug_logging          = false
  engine_family          = "POSTGRESQL"
  idle_client_timeout    = 1800
  require_tls            = true
  role_arn               = data.aws_iam_role.wfnews_automation_role.arn
  vpc_security_group_ids = [module.networking.security_groups.web.id, aws_security_group.wfnews_ecs_tasks.id]
  vpc_subnet_ids         = module.network.aws_subnet_ids.app.ids

  auth {
    iam_auth    = "DISABLED"
    secret_arn  = aws_secretsmanager_secret.wfnews_db_pw_secret.arn
    username = aws_db_instance.wfnews_pgsqlDB.username
  }

  tags = local.common_tags
  depends_on = [
    aws_secretsmanager_secret.wfnews_db_pw_secret
  ]
}

resource "aws_db_proxy_default_target_group" "wfnews_db_target_group" {
  db_proxy_name = aws_db_proxy.wfnews_db_proxy.name
}

resource "aws_db_proxy_target" "wfnews_db_proxy_target" {
  db_instance_identifier = aws_db_instance.wfnews_pgsqlDB.id
  db_proxy_name          = aws_db_proxy.wfnews_db_proxy.name
  target_group_name      = aws_db_proxy_default_target_group.wfnews_db_target_group.name
}
*/
