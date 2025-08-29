resource "aws_ecr_repository" "report_generator" {
  name                 = "report-generator"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  encryption_configuration {
    encryption_type = "AES256"
  }
}