resource "aws_security_group" "wfprev_tomcat_access" {
  name = "wfprev-ecs-tasks-allow-access"
  description = "Explicitly allow traffic on ports used by WFPREV"
  vpc_id = module.networking.vpc.id
  ingress {
    protocol = "tcp"
    from_port = var.WFPREV_CLIENT_PORT
    to_port = var.WFPREV_API_PORT
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "jumphost" {
  name        = "wfprev-jumphost-access"
  description = "Allow access to jumphost via ssm"
  vpc_id      = module.networking.vpc.id
  ingress {
    protocol = "tcp"
    from_port = 3389
    to_port = 3389
    security_groups = [module.networking.security_groups.web.id]
  }

  ingress {
    protocol = "tcp"
    from_port = 3389
    to_port = 3389
    security_groups = [module.networking.security_groups.app.id]
  }
}