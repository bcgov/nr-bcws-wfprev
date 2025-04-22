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

resource "aws_lambda_function" "gdb_processor" {
  function_name = "wfprev-gdb-${var.TARGET_ENV}"
  role          = aws_iam_role.lambda_role.arn
  package_type  = "Image"

  image_uri     = "183631341627.dkr.ecr.ca-central-1.amazonaws.com/nr-bcws-wfprev-wfprev-gdb-extractor:latest"

  memory_size   = var.WFPREV_LAMBDA_MEMORY
  timeout       = var.WFPREV_LAMBDA_TIMEOUT

  environment {
    variables = {
      NODE_ENV = var.TARGET_ENV
    }
  }
}
