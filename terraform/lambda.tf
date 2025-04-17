resource "aws_iam_role" "lambda_role" {
  name = "wfprev-${var.TARGET_ENV}-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

# Lambda basic execution policy (minimum required for Lambda to run)
resource "aws_iam_role_policy_attachment" "lambda_basic" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Lambda function using container image
resource "aws_lambda_function" "gdb_processor" {
  function_name = "wfprev-${var.TARGET_ENV}"
  role          = aws_iam_role.lambda_role.arn
  package_type  = "Image"

  image_uri     = "ghcr.io/bcgov/nr-bcws-wfprev-wfprev-gdb-extractor@sha256:5edbc71bca0cbc90b482998e013bf7d16f6e09c023f7ebf5eaf1cb294cc1a3b5"

  memory_size   = var.WFPREV_LAMBDA_MEMORY
  timeout       = var.WFPREV_LAMBDA_TIMEOUT

  environment {
    variables = {
      NODE_ENV = var.TARGET_ENV
    }
  }
}