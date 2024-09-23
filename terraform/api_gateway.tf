// access externally

resource "aws_apigatewayv2_api" "wfhr_api_gateway" {
  name          = "wfhr-api-gateway-${var.TARGET_ENV}"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_route" "base_route" {
  api_id = aws_apigatewayv2_api.wfhr_api_gateway.id
  route_key = "$default"

  target = "integrations/${aws_apigatewayv2_integration.wfhr_vpc_integration.id}"
}

resource "aws_apigatewayv2_vpc_link" "wfhr_vpc_link" {
  name               = "wfhr-vpc-link-${var.TARGET_ENV}"
  security_group_ids = [data.aws_security_group.web.id]
  subnet_ids         = module.network.aws_subnet_ids.web.ids

  tags = {
    Environment = "${var.TARGET_ENV}"
  }
}

resource "aws_apigatewayv2_integration" "wfhr_vpc_integration" {
  api_id           = aws_apigatewayv2_api.wfhr_api_gateway.id
  description      = "WFHR Payroll API integration"
  integration_type = "HTTP_PROXY"
  integration_uri  = aws_lb_listener.wfhr_main.arn

  integration_method = "ANY"
  connection_type    = "VPC_LINK"
  connection_id      = aws_apigatewayv2_vpc_link.wfhr_vpc_link.id


  response_parameters {
    status_code = 403
    mappings = {
      "append:header.auth" = "$context.authorizer.authorizerResponse"
    }
  }

}

resource "aws_apigatewayv2_stage" "wfhr_stage" {
  api_id = aws_apigatewayv2_api.wfhr_api_gateway.id
  name   = "pub"
}

resource "aws_apigatewayv2_deployment" "wfhr_deployment" {
  api_id = aws_apigatewayv2_api.wfhr_api_gateway.id

  triggers = {
    redeployment = sha1(join(",",tolist([
      jsonencode(aws_apigatewayv2_integration.wfhr_vpc_integration),
      jsonencode(aws_apigatewayv2_route.base_route),
      jsonencode(aws_apigatewayv2_integration.wfhr_vpc_integration),
      jsonencode(aws_apigatewayv2_vpc_link.wfhr_vpc_link)
    ])))
  }
  lifecycle {
    create_before_destroy = true
  }
}