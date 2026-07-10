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


resource "aws_sns_topic_policy" "wfprev_topic_policy" {
  arn    = aws_sns_topic.alb_alerts.arn
  policy = data.aws_iam_policy_document.wfprev_topic_policy_document.json
}

data "aws_iam_policy_document" "wfprev_topic_policy_document" {
  policy_id = "__default_policy_ID"
  statement {
    actions = [
      "SNS:Subscribe",
      "SNS:Receive",
      "SNS:Publish",
      "SNS:ListSubscriptionsByTopic",
      "SNS:GetTopicAttributes"
    ]

    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = ["${data.aws_caller_identity.current.account_id}"]
    }

    principals {
      type        = "Service"
      identifiers = ["events.amazonaws.com"]
    }

    resources = [
      aws_sns_topic.alb_alerts.arn
    ]

    sid = "__default_statement_ID"
  }
}