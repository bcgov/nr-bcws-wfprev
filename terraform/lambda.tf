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

  image_uri = var.WFPREV_GDB_EXTRACTOR_DIGEST

  memory_size   = var.WFPREV_LAMBDA_MEMORY
  timeout       = var.WFPREV_LAMBDA_TIMEOUT

  environment {
    variables = {
      NODE_ENV = var.TARGET_ENV
    }
  }
}

resource "aws_lambda_function" "report_generator" {
  function_name = "report-generator-${var.TARGET_ENV}"
  role          = aws_iam_role.lambda_exec.arn
  package_type  = "Image"

  image_uri     = var.WFPREV_REPORT_GENERATOR_IMAGE
  
  memory_size   = var.WFPREV_LAMBDA_MEMORY
  timeout       = var.WFPREV_LAMBDA_TIMEOUT

  environment {
    variables = {
      NODE_ENV = var.TARGET_ENV
    }
  }
}

# # API Gateway
# resource "aws_apigatewayv2_api" "http_api" {
#   name          = "wfprev-${var.TARGET_ENV}-gdb-api"
#   protocol_type = "HTTP"
# }

# # Integration
# resource "aws_apigatewayv2_integration" "lambda_integration" {
#   api_id                 = aws_apigatewayv2_api.http_api.id
#   integration_type       = "AWS_PROXY"
#   integration_uri        = aws_lambda_function.gdb_processor.invoke_arn
#   integration_method     = "POST"
#   payload_format_version = "2.0"
# }

# # Route
# resource "aws_apigatewayv2_route" "upload_route" {
#   api_id    = aws_apigatewayv2_api.http_api.id
#   route_key = "POST /upload"
#   target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
# }

# # Deployment Stage
# resource "aws_apigatewayv2_stage" "default" {
#   api_id      = aws_apigatewayv2_api.http_api.id
#   name        = "$default"
#   auto_deploy = true
# }

# # Lambda Permission
# resource "aws_lambda_permission" "allow_apigw" {
#   statement_id  = "AllowAPIGatewayInvoke"
#   action        = "lambda:InvokeFunction"
#   function_name = aws_lambda_function.gdb_processor.function_name
#   principal     = "apigateway.amazonaws.com"
#   source_arn    = "${aws_apigatewayv2_api.http_api.execution_arn}/*/*"
# }

# # catch-all debug route
# resource "aws_apigatewayv2_route" "catch_all" {
#   api_id    = aws_apigatewayv2_api.http_api.id
#   route_key = "ANY /{proxy+}"
#   target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
# }

# # Output
# output "api_gateway_url" {
#   value = "${aws_apigatewayv2_api.http_api.api_endpoint}"
# }
