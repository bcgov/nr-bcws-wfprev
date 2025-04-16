resource "aws_cloudwatch_log_group" "api_logs" {
  name              = "/aws/apigateway/${aws_apigatewayv2_stage.wfprev_stage.name}"
  retention_in_days = 90

  lifecycle {
    prevent_destroy = true
  }
}