resource "aws_cloudwatch_metric_alarm" "high_latency" {
  alarm_name          = "ALB-High-Latency"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "TargetResponseTime"
  namespace           = "AWS/ApplicationELB"
  period              = 60  # 1 min
  statistic           = "Average"
  threshold           = 1  # 1 second

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

resource "aws_sns_topic" "alb_alerts" {
  name = "wfprev-alb-alerts"
}

# List of emails
locals {
  alert_emails = split(",", var.AWS_ALERT_EMAIL_LIST)
}

# Create subscriptions for each email
resource "aws_sns_topic_subscription" "alb_alerts_emails" {
  for_each = toset(local.alert_emails)

  topic_arn = aws_sns_topic.alb_alerts.arn
  protocol  = "email"
  endpoint  = trim(each.key) 
}