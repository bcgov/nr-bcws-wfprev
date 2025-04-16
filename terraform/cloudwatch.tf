resource "aws_cloudwatch_log_group" "api_logs" {
  name              = "/aws/apigateway/wfprev-dev"
  retention_in_days = 90

  lifecycle {
    prevent_destroy = true
  }
}