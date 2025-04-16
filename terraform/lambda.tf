# Lambda execution role
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

# Lambda basic execution policy
resource "aws_iam_role_policy_attachment" "lambda_basic" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Optional: S3 access policy
resource "aws_iam_policy" "s3_access" {
  name        = "wfprev-${var.TARGET_ENV}-s3-access"
  description = "Allow Lambda to access S3 bucket"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:ListBucket"
        ]
        Effect   = "Allow"
        Resource = [
          "arn:aws:s3:::wfprev-${var.TARGET_ENV}-lambda-bucket",
          "arn:aws:s3:::wfprev-${var.TARGET_ENV}-lambda-bucket/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "s3_access" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.s3_access.arn
}

# Lambda function using container image
resource "aws_lambda_function" "gdb_processor" {
  function_name = "wfprev-${var.TARGET_ENV}"
  role          = aws_iam_role.lambda_role.arn
  package_type  = "Image"
  
  image_uri     = "ghcr.io/bcgov/nr-bcws-wfprev-wfprev-gdb-extractor:latest"

  memory_size   = var.WFPREV_LAMBDA_MEMORY
  timeout       = var.WFPREV_LAMBDA_TIMEOUT

  environment {
    variables = {
      ALLOWED_ORIGINS = "https://wfprev-dev.nrs.gov.bc.ca"
      NODE_ENV        = var.TARGET_ENV
    }
  }
}

# API Gateway
resource "aws_apigatewayv2_api" "api" {
  name          = "wfprev-${var.TARGET_ENV}-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["https://wfprev-dev.nrs.gov.bc.ca"]
    allow_methods = ["POST", "GET", "OPTIONS"]
    allow_headers = ["content-type", "x-amz-date", "authorization", "x-api-key", "x-amz-security-token"]
    max_age       = 300
  }
}

resource "aws_apigatewayv2_integration" "lambda_integration" {
  api_id                 = aws_apigatewayv2_api.api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.gdb_processor.invoke_arn
  integration_method     = "POST"
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "upload_route" {
  api_id    = aws_apigatewayv2_api.api.id
  route_key = "POST /upload"
  target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
}

resource "aws_apigatewayv2_route" "health_route" {
  api_id    = aws_apigatewayv2_api.api.id
  route_key = "GET /health"
  target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
}

# Lambda permission for API Gateway
resource "aws_lambda_permission" "api_gateway" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.gdb_processor.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.api.execution_arn}/*/*"
}