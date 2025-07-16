// access externally
module api_gateway {
  source = "git::https://github.com/bcgov/quickstart-aws-helpers.git//terraform/modules/api-gateway?ref=v0.0.5"

  api_name = "wfprev-api-gateway-${var.TARGET_ENV}"

  #VPC Link
  vpc_link_name = "wfprev-vpc-link-${var.TARGET_ENV}"
  security_group_ids = [module.networking.security_groups.web.id]
  subnet_ids         = module.networking.subnets.web.ids

  #Integration
  integration_type = "HTTP_PROXY"
  integration_method = "ANY"
  integration_uri  = aws_lb_listener.wfprev_main.arn

  #Base Route
  route_key = "$default"

  #Stage
  stage_name   = "wfprev-api"

  enable_cors = true
}