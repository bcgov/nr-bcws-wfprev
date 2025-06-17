// access externally

resource "aws_apigatewayv2_api" "wfprev_api_gateway" {
  name          = "wfprev-api-gateway-${var.TARGET_ENV}"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_route" "base_route" {
  api_id = aws_apigatewayv2_api.wfprev_api_gateway.id
  route_key = "$default"

  target = "integrations/${aws_apigatewayv2_integration.wfprev_vpc_integration.id}"
  authorization_type = "NONE"
  api_key_required   = true
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
}
resource "aws_apigatewayv2_deployment" "wfprev_deployment" {
  
  api_id = aws_apigatewayv2_api.wfprev_api_gateway.id

  triggers = {
    redeployment = sha1(join(",",tolist([
      jsonencode(aws_apigatewayv2_stage.wfprev_stage),
      jsonencode(aws_apigatewayv2_integration.wfprev_vpc_integration),
      jsonencode(aws_apigatewayv2_route.base_route),
      jsonencode(aws_apigatewayv2_integration.wfprev_vpc_integration),
      jsonencode(aws_apigatewayv2_vpc_link.wfprev_vpc_link)
    ])))
  }
  lifecycle {
    create_before_destroy = true
  }
}
// API Key
resource "aws_apigatewayv2_api_key" "wfprev_api_key" {
  name      = "wfprev-api-key-${var.TARGET_ENV}"
  enabled   = true
  value     = var.API_KEY
}
// Usage Plan (Throttling & Quotas)
resource "aws_apigatewayv2_usage_plan" "wfprev_usage_plan" {
  name = "wfprev-usage-plan-${var.TARGET_ENV}"

  api_stages {
    api_id = aws_apigatewayv2_api.wfprev_api_gateway.id
    stage  = aws_apigatewayv2_stage.wfprev_stage.name
  }

  throttle {
    rate_limit  = 100   # requests per second
    burst_limit = 200   # max burst capacity
  }

  quota {
    limit  = 10000      # total requests allowed per period
    period = "MONTH"
  }
}

 //Attach API Key to Usage Plan
resource "aws_apigatewayv2_usage_plan_key" "wfprev_api_key_attachment" {
  key_id        = aws_apigatewayv2_api_key.wfprev_api_key.id
  key_type      = "API_KEY"
  usage_plan_id = aws_apigatewayv2_usage_plan.wfprev_usage_plan.id
}