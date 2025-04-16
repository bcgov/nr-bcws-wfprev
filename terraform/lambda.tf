resource "aws_iam_role" "lambda_exec_role" {
  name = "lambda_exec_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Principal = {
        Service = "lambda.amazonaws.com"
      },
      Effect = "Allow",
      Sid    = ""
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.lambda_exec_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_lambda_function" "gdb_extractor" {
  function_name = "wfprev-gdb-extractor"
  package_type  = "Image"
  image_uri     = "ghcr.io/bcgov/nr-bcws-wfprev-wfprev-gdb-extractor:latest"
  role          = aws_iam_role.lambda_exec_role.arn
  timeout       = 30
  memory_size   = 512
  publish       = true

  environment {
    variables = {
      NODE_ENV = "dev"
    }
  }
}
