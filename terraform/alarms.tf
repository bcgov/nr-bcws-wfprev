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
}

resource "aws_config_config_rule" "acm_cert_expiration" {
  name = "acm-certificate-expiration-check"
  source {
    owner             = "AWS"
    source_identifier = "ACM_CERTIFICATE_EXPIRATION_CHECK"
  }

  input_parameters = jsonencode({
    daysToExpiration = 30  # Alarm if cert expires within 30 days
  })
}