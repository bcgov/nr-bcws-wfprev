resource "aws_cloudwatch_metric_alarm" "high_latency" {
  alarm_name          = "ALB-High-Latency"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "TargetResponseTime"
  namespace           = "AWS/ApplicationELB"
  period              = 60 # 1 min
  statistic           = "Average"
  threshold           = 3 # 3 seconds

  dimensions = {
    LoadBalancer = aws_lb.wfprev_main.arn_suffix
  }

  alarm_description = "ALB target response time is high"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

resource "aws_cloudwatch_metric_alarm" "unhealthy_targets" {
  alarm_name          = "ALB-Unhealthy-Targets"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "UnHealthyHostCount"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Average"
  threshold           = 0

  dimensions = {
    TargetGroup  = aws_alb_target_group.wfprev_api.arn_suffix
    LoadBalancer = aws_lb.wfprev_main.arn_suffix
  }

  alarm_description = "ALB has unhealthy targets"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

# ECS CPU Utilization
resource "aws_cloudwatch_metric_alarm" "ecs_high_cpu" {
  alarm_name          = "ECS-High-CPU"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 5
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 80

  dimensions = {
    ClusterName = aws_ecs_cluster.wfprev_main.name
    ServiceName = aws_ecs_service.wfprev_server.name
  }

  alarm_description = "ECS Service CPU utilization is high"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

# ECS Memory Utilization
resource "aws_cloudwatch_metric_alarm" "ecs_high_memory" {
  alarm_name          = "ECS-High-Memory"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 5
  metric_name         = "MemoryUtilization"
  namespace           = "AWS/ECS"
  period              = 60
  statistic           = "Average"
  threshold           = 85

  dimensions = {
    ClusterName = aws_ecs_cluster.wfprev_main.name
    ServiceName = aws_ecs_service.wfprev_server.name
  }

  alarm_description = "ECS Service Memory utilization is high"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

# RDS CPU Utilization
resource "aws_cloudwatch_metric_alarm" "rds_high_cpu" {
  alarm_name          = "RDS-High-CPU"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 5
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 60
  statistic           = "Average"
  threshold           = 80

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.wfprev_pgsqlDB.identifier
  }

  alarm_description = "RDS CPU utilization is high"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

# RDS Freeable Memory (Low Memory)
resource "aws_cloudwatch_metric_alarm" "rds_low_memory" {
  alarm_name          = "RDS-Low-Memory"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 5
  metric_name         = "FreeableMemory"
  namespace           = "AWS/RDS"
  period              = 60
  statistic           = "Average"
  threshold           = var.ALARM_RDS_FREEABLE_MEMORY_THRESHOLD_BYTES

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.wfprev_pgsqlDB.identifier
  }

  alarm_description = "RDS Freeable Memory is low"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

# RDS Disk Read Throughput
resource "aws_cloudwatch_metric_alarm" "rds_high_disk_read" {
  alarm_name          = "RDS-High-Disk-Read"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ReadThroughput"
  namespace           = "AWS/RDS"
  period              = 60
  statistic           = "Average"
  threshold           = 17895697 # ~1GB/min in bytes/sec

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.wfprev_pgsqlDB.identifier
  }

  alarm_description = "RDS Disk Read throughput is high"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

# RDS Disk Write Throughput
resource "aws_cloudwatch_metric_alarm" "rds_high_disk_write" {
  alarm_name          = "RDS-High-Disk-Write"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "WriteThroughput"
  namespace           = "AWS/RDS"
  period              = 60
  statistic           = "Average"
  threshold           = 17895697 # ~1GB/min in bytes/sec

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.wfprev_pgsqlDB.identifier
  }

  alarm_description = "RDS Disk Write throughput is high"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

# RDS Network In
resource "aws_cloudwatch_metric_alarm" "rds_high_network_in" {
  alarm_name          = "RDS-High-Network-In"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "NetworkReceiveThroughput"
  namespace           = "AWS/RDS"
  period              = 3600
  statistic           = "Average"
  threshold           = 2982616 # ~10GB/hour in bytes/sec

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.wfprev_pgsqlDB.identifier
  }

  alarm_description = "RDS Network In traffic is high"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

# RDS Network Out
resource "aws_cloudwatch_metric_alarm" "rds_high_network_out" {
  alarm_name          = "RDS-High-Network-Out"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "NetworkTransmitThroughput"
  namespace           = "AWS/RDS"
  period              = 3600
  statistic           = "Average"
  threshold           = 2982616 # ~10GB/hour in bytes/sec

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.wfprev_pgsqlDB.identifier
  }

  alarm_description = "RDS Network Out traffic is high"
  alarm_actions     = [aws_sns_topic.alb_alerts.arn]
}

resource "aws_sns_topic" "alb_alerts" {
  name = var.SNS_TOPIC_NAME
}

# List of emails
locals {
  alert_emails = split(",", var.AWS_ALERT_EMAIL_LIST)
}

# Create email subscriptions for SNS topic
resource "aws_sns_topic_subscription" "alb_alerts_emails" {
  for_each = toset(local.alert_emails)

  topic_arn = aws_sns_topic.alb_alerts.arn
  protocol  = "email"
  endpoint  = trim(each.key, " ")
}
