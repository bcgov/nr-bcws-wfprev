# alb.tf

# Must use a pre-existing ALB, such as default that is pre-provisioned as part of the account creation
# This ALB has all traffic on *.LICENSE-PLATE-ENV.nimbus.cloud.gob.bc.ca routed to it

# data "aws_lb" "wfhr_main" {
#   name = var.alb_name
# }


# # Redirect all traffic from the ALB to the target group
# data "aws_alb_listener" "wfhr_main_front_end" {
#   load_balancer_arn = data.aws_lb.wfhr_main.id
#   port              = 443
# }

#Only use "example cert for proof-of-concept - will need real cert for proper implementation"
data "aws_acm_certificate" "example" {
  domain = "*.example.ca"
  statuses = ["ISSUED"]
}

resource "aws_lb" "wfhr_main" {
  name               = var.ALB_NAME
  internal           = true
  load_balancer_type = "application"
  security_groups    = [data.aws_security_group.web.id]
  subnets            = module.network.aws_subnet_ids.web.ids

  enable_deletion_protection = true

  tags = {
    Environment = "${var.TARGET_ENV}"
  }

}

# resource "aws_lb_listener" "wfhr_main" {
#   load_balancer_arn = "${aws_lb.wfhr_main.arn}"
#   port              = "443"
#   protocol          = "HTTPS"
#   ssl_policy        = "ELBSecurityPolicy-2016-08"
#   certificate_arn   = data.aws_acm_certificate.example.arn

#   default_action {
#     type             = "fixed-response"
#     fixed_response {
#       content_type = "text/plain"
#       status_code = 200
#     }
#   }
# }


//////////////////////////
/// LISTENER RESOURCES ///
//////////////////////////
resource "aws_lb_listener" "wfhr_main" {
  load_balancer_arn = "${aws_lb.wfhr_main.arn}"
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "fixed-response"
    fixed_response {
      content_type = "text/plain"
      status_code = 404
    }
  }
}

resource "aws_lb_listener_rule" "wfhr_payroll_api_host_based_weighted_routing" {
  listener_arn = aws_lb_listener.wfhr_main.arn

  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.wfhr_payroll_api.arn
  }

  condition {
    path_pattern  {
      values = [for sn in var.PAYROLL_API_NAMES : "/${aws_apigatewayv2_stage.wfhr_stage.name}/${sn}"]
    }
  }
#  condition {
#    http_header {
#      http_header_name = "X-Cloudfront-Header"
#      values           = ["${var.cloudfront_header}"]
#    }
#  }
}

resource "aws_lb_listener_rule" "wfhr_ediaries_war_host_based_weighted_routing" {
  listener_arn = aws_lb_listener.wfhr_main.arn

  action {
    type = "forward"
    target_group_arn = aws_alb_target_group.wfhr_ediaries_war.arn
  }

  condition {
    path_pattern  {
      values = [for sn in var.EDIARIES_WAR_NAMES : "/${aws_apigatewayv2_stage.wfhr_stage.name}/${sn}"]
    }
  }
}

resource "aws_lb_listener_rule" "wfhr_earnings_war_host_based_weighted_routing" {
  listener_arn = aws_lb_listener.wfhr_main.arn

  action {
    type = "forward"
    target_group_arn = aws_alb_target_group.wfhr_earnings_war.arn
  }

  condition {
    path_pattern  {
      values = [for sn in var.EARNINGS_WAR_NAMES : "/${aws_apigatewayv2_stage.wfhr_stage.name}/${sn}"]
    }
  }
}

resource "aws_lb_listener_rule" "wfhr_earnings_api_host_based_weighted_routing" {
  listener_arn = aws_lb_listener.wfhr_main.arn

  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.wfhr_earnings_api.arn
  }

  condition {
    path_pattern  {
      values = [for sn in var.EARNINGS_API_NAMES : "/${aws_apigatewayv2_stage.wfhr_stage.name}/${sn}"]
    }
  }
}

resource "aws_lb_listener_rule" "wfhr_diary_submission_listener_api_host_based_weighted_routing" {
  listener_arn = aws_lb_listener.wfhr_main.arn

  action {
    type = "forward"
    target_group_arn = aws_alb_target_group.wfhr_diary_submission_listener_api.arn
  }

  condition {
    path_pattern  {
      values = [for sn in var.DIARY_SUBMISSION_LISTENER_API_NAMES : "/${aws_apigatewayv2_stage.wfhr_stage.name}/${sn}"]
    }
  }

}


resource "aws_lb_listener_rule" "wfhr_earnings_submission_listener_api_host_based_weighted_routing" {
  listener_arn = aws_lb_listener.wfhr_main.arn

  action {
    type = "forward"
    target_group_arn = aws_alb_target_group.wfhr_earnings_submission_listener_api.arn
  }

  condition {
    path_pattern  {
      values = [for sn in var.EARNINGS_SUBMISSION_LISTENER_API_NAMES : "/${aws_apigatewayv2_stage.wfhr_stage.name}/${sn}"]
    }
  }

}


resource "aws_lb_listener_rule" "wfhr_chips_sync_api_host_based_weighted_routing" {
  listener_arn = aws_lb_listener.wfhr_main.arn
        action {
    type = "forward"
    target_group_arn = aws_alb_target_group.wfhr_chips_sync_api.arn
  }

  condition {
    path_pattern  {
      values = [for sn in var.CHIPS_SYNC_NAMES : "/${aws_apigatewayv2_stage.wfhr_stage.name}/${sn}"]
    }
  }
}

resource "aws_lb_listener_rule" "wfhr_psa_chips_data_stub_api_host_based_weighted_routing" {
  listener_arn = aws_lb_listener.wfhr_main.arn

  action {
    type = "forward"
    target_group_arn = aws_alb_target_group.wfhr_psa_chips_data_stub_api.arn
  }

  condition {
    path_pattern  {
      values = [for sn in var.PSA_CHIPS_STUB_NAMES : "/${aws_apigatewayv2_stage.wfhr_stage.name}/${sn}"]
    }
  }
}

resource "aws_lb_listener_rule" "wfhr_jasper_reports_server" {
  // MUST have listener for target group to exist, but can set condition which will always be false
  listener_arn = aws_lb_listener.wfhr_main.arn
  action {
    type = "forward"
    target_group_arn = aws_alb_target_group.wfhr_jasper_reports_server.arn
  }

  condition {
    #NOTE: can still only be accessed from inside appropriate security group, despite listener
    source_ip {
      values = [for sn in module.network.aws_subnet.app : sn.cidr_block]
    }
  }

  condition {
    path_pattern {
      values = [for sn in var.JASPER_REPORTS_SERVER_NAMES : "/${aws_apigatewayv2_stage.wfhr_stage.name}/${sn}"]
    }
  }
  
}

//////////////////////////////
/// TARGET GROUP RESOURCES ///
//////////////////////////////
resource "aws_alb_target_group" "wfhr_payroll_api" {
  name                 = "wfhr-payroll-api-${var.TARGET_ENV}"
  port                 = var.PAYROLL_API_PORT
  protocol             = "HTTP"
  vpc_id               = module.network.aws_vpc.id
  target_type          = "ip"
  deregistration_delay = 30

  health_check {
    healthy_threshold   = "2"
    interval            = "300"
    protocol            = "HTTP"
    matcher             = "200"
    timeout             = "3"
    path                = "/${aws_apigatewayv2_stage.wfhr_stage.name}/wfhr-payroll-api/checkHealth?callstack=test"
    unhealthy_threshold = "2"
  }

#  tags = local.common_tags
}

resource "aws_alb_target_group" "wfhr_ediaries_war" {
  name = "wfhr-ediaries-war-${var.TARGET_ENV}"
  port = var.EDIARIES_WAR_PORT
  protocol = "HTTP"
  vpc_id = module.network.aws_vpc.id
  target_type = "ip"
  deregistration_delay = 30

  health_check {
    healthy_threshold   = "2"
    interval            = "300"
    protocol            = "HTTP"
    matcher             = "200"
    timeout             = "3"
    path                = "/${aws_apigatewayv2_stage.wfhr_stage.name}/wfhr-ediaries-ui/"
    unhealthy_threshold = "2"
  }
}

resource "aws_alb_target_group" "wfhr_earnings_war" {
  name = "wfhr-earnings-war-${var.TARGET_ENV}"
  port = var.EDIARIES_WAR_PORT
  protocol = "HTTP"
  vpc_id = module.network.aws_vpc.id
  target_type = "ip"
  deregistration_delay = 30

  health_check {
    healthy_threshold   = "2"
    interval            = "300"
    protocol            = "HTTP"
    matcher             = "200"
    timeout             = "3"
    path                = "/${aws_apigatewayv2_stage.wfhr_stage.name}/wfhr-earnings/"
    unhealthy_threshold = "2"
  }
}

resource "aws_alb_target_group" "wfhr_chips_sync_api" {
  name = "wfhr-chips-sync-api-${var.TARGET_ENV}"
  port = var.CHIPS_SYNC_PORT
    protocol = "HTTP"
  vpc_id = module.network.aws_vpc.id
  target_type = "ip"
  deregistration_delay = 30

  health_check {
    healthy_threshold   = "2"
    interval            = "300"
    protocol            = "HTTP"
    matcher             = "200"
    timeout             = "3"
        path                = "/${aws_apigatewayv2_stage.wfhr_stage.name}/wfhr-chips-sync-api/checkHealth?callstack=test"
    unhealthy_threshold = "2"
  }
}

resource "aws_alb_target_group" "wfhr_psa_chips_data_stub_api" {
  name = "wfhr-psa-chips-data-stub-api-${var.TARGET_ENV}"
  port = var.PSA_CHIPS_STUB_PORT
  protocol = "HTTP"
  vpc_id = module.network.aws_vpc.id
  target_type = "ip"
  deregistration_delay = 30

  health_check {
    healthy_threshold   = "2"
    interval            = "300"
    protocol            = "HTTP"
    #Does not have checkHealth endpoint and API is secure - expect 401 on any request without credentials
    matcher             = "401"
    timeout             = "3"
    path                = "/${aws_apigatewayv2_stage.wfhr_stage.name}/wfhr-psa-chips-data-stub-api"
    unhealthy_threshold = "2"
  }
}

resource "aws_alb_target_group" "wfhr_earnings_api" {
  name                 = "wfhr-earnings-api-${var.TARGET_ENV}"
  port                 = var.EARNINGS_API_PORT
  protocol             = "HTTP"
  vpc_id               = module.network.aws_vpc.id
  target_type          = "ip"

  deregistration_delay = 30

  health_check {
    healthy_threshold   = "2"
    interval            = "300"
    protocol            = "HTTP"
    matcher             = "200"
    timeout             = "3"
    path                = "/${aws_apigatewayv2_stage.wfhr_stage.name}/wfhr-earnings-api/checkHealth?callstack=test"
    unhealthy_threshold = "2"
  }
}


resource "aws_alb_target_group" "wfhr_diary_submission_listener_api" {
  name = "wfhr-diary-submission-lstnr-${var.TARGET_ENV}"
  port = var.DIARY_SUBMISSION_LISTENER_API_PORT
  protocol = "HTTP"
  vpc_id = module.network.aws_vpc.id
  target_type = "ip"
  deregistration_delay = 30

  health_check {
    healthy_threshold   = "2"
    interval            = "300"
    protocol            = "HTTP"
    #TODO: Replace matcher and path with correct values once checkHealth endpoint available
    matcher             = "200"
    timeout             = "3"
    path                = "/${aws_apigatewayv2_stage.wfhr_stage.name}/wfhr-diary-submission-listener-api/checkHealth?callstack=test"
    unhealthy_threshold = "2"
  }
}

resource "aws_alb_target_group" "wfhr_jasper_reports_server" {
  name = "wfhr-jasper-reports-${var.TARGET_ENV}"
  port = var.JASPER_REPORTS_SERVER_PORT
  protocol = "HTTP"
  vpc_id = module.network.aws_vpc.id
  target_type = "ip"
  deregistration_delay = 30

  health_check {
    healthy_threshold   = "2"
    interval            = "300"
    protocol            = "HTTP"
    #TODO: Replace matcher and path with correct values once checkHealth endpoint available
    matcher             = "200"
    timeout             = "3"
    path                = "/jasperserver/"
    unhealthy_threshold = "2"
  }
}

resource "aws_alb_target_group" "wfhr_earnings_submission_listener_api" {
  name = "wfhr-earn-submission-lstnr-${var.TARGET_ENV}"
  port = var.EARNINGS_SUBMISSION_LISTENER_API_PORT
  protocol = "HTTP"
  vpc_id = module.network.aws_vpc.id
  target_type = "ip"
  deregistration_delay = 30

  health_check {
    healthy_threshold   = "2"
    interval            = "300"
    protocol            = "HTTP"
    #TODO: Replace matcher and path with correct values once checkHealth endpoint available
    matcher             = "200"
    timeout             = "3"
    path                = "/${aws_apigatewayv2_stage.wfhr_stage.name}/wfhr-earnings-submission-listener-api/checkHealth?callstack=test"
    unhealthy_threshold = "2"
  }
}