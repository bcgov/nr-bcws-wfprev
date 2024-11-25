# alb.tf

# Must use a pre-existing ALB, such as default that is pre-provisioned as part of the account creation
# This ALB has all traffic on *.LICENSE-PLATE-ENV.nimbus.cloud.gob.bc.ca routed to it

# data "aws_lb" "wfprev_main" {
#   name = var.alb_name
# }


# # Redirect all traffic from the ALB to the target group
# data "aws_alb_listener" "wfprev_main_front_end" {
#   load_balancer_arn = data.aws_lb.wfprev.id
#   port              = 443
# }

#Only use "example cert for proof-of-concept - will need real cert for proper implementation"
// do we need this?????
# data "aws_acm_certificate" "example" {
#   domain = "*.example.ca"
#   statuses = ["ISSUED"]
# }

resource "aws_lb" "wfprev_main" {
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

//////////////////////////
/// LISTENER RESOURCES ///
//////////////////////////
resource "aws_lb_listener" "wfprev_main" {
  load_balancer_arn = "${aws_lb.wfprev_main.arn}"
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

/// LISTENER RULES////

resource "aws_lb_listener_rule" "wfprev-api" {
  listener_arn = aws_lb_listener.wfprev_main.arn

  action {
    type = "forward"
    target_group_arn = aws_alb_target_group.wfprev_api.arn
  }

  condition {
    path_pattern  {
      values = [for sn in var.PREVENTION_API_NAMES : "/${aws_apigatewayv2_stage.wfprev_stage.name}/${sn}"]
    }
  }
}

//////////////////////////////
/// TARGET GROUP RESOURCES ///
//////////////////////////////
resource "aws_alb_target_group" "wfprev_api" {
  name                 = "wfprev-api-${var.TARGET_ENV}"
  port                 = var.WFPREV_API_PORT
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
    path                = "/${aws_apigatewayv2_stage.wfprev_stage.name}/wfprev-api/actuator/health"
    unhealthy_threshold = "2"
  }

#  tags = local.
}
