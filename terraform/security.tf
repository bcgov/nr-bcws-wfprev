data "aws_security_group" "web" {
  name = "Web_sg"
}

data "aws_security_group" "app" {
  name = "App_sg"
}

data "aws_security_group" "data" {
  name = "Data_sg"
}
resource "aws_security_group" "wfprev_tomcat_access" {
  name = "wfprev-ecs-tasks-allow-access"
  description = "Explicitly allow traffic on ports used by WFPREV"
  vpc_id = module.network.aws_vpc.id
  ingress {
    protocol = "tcp"
    from_port = var.WFPREV_CLIENT_PORT
    to_port = var.WFPREV_API_PORT
    cidr_blocks = ["0.0.0.0/0"]
  }
}