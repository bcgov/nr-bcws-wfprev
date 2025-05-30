data "aws_ami" "amzn-linux-2023-ami" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

resource "aws_instance" "jumphost" {
  ami           = data.aws_ami.amzn-linux-2023-ami.id
  instance_type = "t2.micro"
  subnet_id = module.network.aws_subnet_ids.app.ids[0]
  vpc_security_group_ids = [data.aws_security_group.app.id, aws_security_group.jumphost.id]
  ebs_optimized = false
  ebs_block_device {
    device_name = "/dev/xvda"
    encrypted = true
    volume_size = 8
  }

  tags = {
    Name = "jumphost-${var.TARGET_ENV}"
  }
}