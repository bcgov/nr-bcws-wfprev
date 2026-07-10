resource "aws_cloudwatch_event_rule" "aws_health_event_rule" {
  name        = "capture-aws-health-notifications"
  description = "Capture AWS Health Notifications"

  event_pattern = jsonencode({
  "$or": [{
    "source": ["aws.health"],
    "detail-type": ["AWS Service Event via CloudTrail"],
    "detail": {
      "eventSource": ["health.amazonaws.com"]
    }
  }, {
    "source": ["aws.health"],
    "detail-type": ["AWS Health Event"]
  }]
})
}

resource "aws_cloudwatch_event_target" "aws_health_event_target" {
  rule      = aws_cloudwatch_event_rule.aws_health_event_rule.name
  target_id = "aws-health-event-${var.SHORTENED_ENV}-target"
  arn       = aws_sns_topic.alb_alerts.arn
}