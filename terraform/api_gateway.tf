// access externally

resource "aws_apigatewayv2_api" "wfprev_api_gateway" {
  name          = "wfprev-api-gateway-${var.TARGET_ENV}"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_route" "base_route" {
  api_id = aws_apigatewayv2_api.wfprev_api_gateway.id
  route_key = "$default"

  target = "integrations/${aws_apigatewayv2_integration.wfprev_vpc_integration.id}"
}

resource "aws_apigatewayv2_vpc_link" "wfprev_vpc_link" {
  name               = "wfprev-vpc-link-${var.TARGET_ENV}"
  security_group_ids = [data.aws_security_group.web.id]
  subnet_ids         = module.network.aws_subnet_ids.web.ids

  tags = {
    Environment = "${var.TARGET_ENV}"
  }
}

resource "aws_apigatewayv2_integration" "wfprev_vpc_integration" {
  api_id           = aws_apigatewayv2_api.wfprev_api_gateway.id
  description      = "WFPREV API integration"
  integration_type = "HTTP_PROXY"
  integration_uri  = aws_lb_listener.wfprev_main.arn

  integration_method = "ANY"
  connection_type    = "VPC_LINK"
  connection_id      = aws_apigatewayv2_vpc_link.wfprev_vpc_link.id


  response_parameters {
    status_code = 403
    mappings = {
      "append:header.auth" = "$context.authorizer.authorizerResponse"
    }
  }

}

resource "aws_apigatewayv2_stage" "wfprev_stage" {
  api_id = aws_apigatewayv2_api.wfprev_api_gateway.id
  name   = "wfprev-api"
  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gw_logs.arn
    format = jsonencode({
      requestId       = "$context.requestId"
      sourceIp        = "$context.identity.sourceIp"
      requestTime     = "$context.requestTime"
      httpMethod      = "$context.httpMethod"
      path            = "$context.path"
      status          = "$context.status"
      protocol        = "$context.protocol"
    })
  }
}
resource "aws_apigatewayv2_deployment" "wfprev_deployment" {
  
  api_id = aws_apigatewayv2_api.wfprev_api_gateway.id

  triggers = {
    redeployment = sha1(join(",",tolist([
      jsonencode(aws_apigatewayv2_stage.wfprev_stage),
      jsonencode(aws_apigatewayv2_integration.wfprev_vpc_integration),
      jsonencode(aws_apigatewayv2_route.base_route),
      jsonencode(aws_apigatewayv2_vpc_link.wfprev_vpc_link)
    ])))
  }
  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_cloudwatch_log_group" "api_gw_logs" {
  name = "/aws/apigateway/${var.TARGET_ENV}-wfprev"
  retention_in_days = 14
}
