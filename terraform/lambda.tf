resource "aws_lambda_function" "gdb_extractor" {
  function_name = "wfprev-gdb-extractor"
  package_type  = "Image"
  image_uri     = "ghcr.io/bcgov/nr-bcws-wfprev-wfprev-gdb-extractor:latest"
  role          = "arn:aws:iam::183631341627:role/github-actions-role"
  timeout       = 30
  memory_size   = 512
  publish       = true

  environment {
    variables = {
      NODE_ENV = var.TARGET_ENV
    }
  }
}