data "aws_iam_policy_document" "ecs_task_execution_role" {
  version = "2012-10-17"
  statement {
    sid     = ""
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}


resource "aws_iam_role" "wfprev_app_container_role" {
  name = "wfprev_app_container_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_role" "wfprev_ecs_task_execution_role" {
  name               = var.ecs_task_execution_role_name
  assume_role_policy = data.aws_iam_policy_document.ecs_task_execution_role.json
}

resource "aws_iam_role_policy_attachment" "wfprev_ecs_task_execution_changelogs" {
  role       = aws_iam_role.wfprev_ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "wfprev_ecs_task_execution_cwlogs" {
  name = "ecs_task_execution_cwlogs"
  role = aws_iam_role.wfprev_ecs_task_execution_role.id

  policy = <<-EOF
  {
      "Version": "2012-10-17",
      "Statement": [
          {
              "Effect": "Allow",
              "Action": [
                  "logs:CreateLogGroup"
              ],
              "Resource": [
                  "arn:aws:logs:*:*:*"
              ]
          }
      ]
  }
EOF
}

# Define an IAM policy to allow access to the SSM parameter
# This policy grants permissions to retrieve the specified SecureString parameter.
resource "aws_iam_policy" "ssm_parameter_access" {
  name        = "SSMParameterAccess"
  description = "Allows access to SecureString parameters in SSM Parameter Store"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "ssm:GetParameter",
          "ssm:GetParameters",
          "ssm:DescribeParameters"
        ],
        Resource = "arn:aws:ssm:ca-central-1:${var.TARGET_AWS_ACCOUNT_ID}:parameter/iam_users/wfprev_github_actions_user_keys"
      }
    ]
  })
}

# Attach the SSM parameter access policy to the GitHub Actions IAM user
# This links the user with the necessary permissions to read the SSM parameter securely.
resource "aws_iam_user_policy_attachment" "ssm_parameter_access_attachment" {
  user       = "wfprev_github_actions_user"
  policy_arn = aws_iam_policy.ssm_parameter_access.arn
}

# Define an IAM policy for GitHub Actions user to perform specific operations
# This policy grants permissions to:
# - Upload/delete objects in an S3 bucket
# - Invalidate cached content in CloudFront
resource "aws_iam_user_policy" "github_actions_policy" {
  name = "github-actions-policy"
  user = "wfprev_github_actions_user"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = ["s3:PutObject", "s3:DeleteObject"],
        Resource = "${module.s3_secure_bucket.bucket_arn}/*"
      },
      {
        Effect   = "Allow",
        Action   = "cloudfront:CreateInvalidation",
        Resource = "*"
      }
    ]
  })
}

# Create an IAM role for GitHub Actions to assume
resource "aws_iam_role" "github_actions_role" {
  name = "github-actions-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = "${data.aws_iam_openid_connect_provider.github_openid_connect_provider.arn}"
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "${data.aws_iam_openid_connect_provider.github_openid_connect_provider.url}:aud" : "sts.amazonaws.com"
          },
          StringLike = {
            "${data.aws_iam_openid_connect_provider.github_openid_connect_provider.url}:sub" : "repo:bcgov/nr-bcws-wfprev:*"
          }
        }
      },
      {
        Sid = "AllowIAMRoleAssume",
        Effect = "Allow",
        Principal = {
          AWS = [
            "arn:aws:iam::${var.TARGET_AWS_ACCOUNT_ID}:role/client_s3_push"
          ]
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_policy" "github_actions_policy" {
  name        = "github-actions-policy"
  description = "Policy for GitHub Actions"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = [
          "s3:ListBucket",   # Bucket-level operations
          "s3:GetObject",    # Object read
          "s3:PutObject",    # Object write
          "s3:DeleteObject",  # Object deletion
          "cloudfront:CreateInvalidation" # Invalidate cache
        ],
        Resource = [
          "arn:aws:s3:::wfprev-${var.SHORTENED_ENV}-site",        # Bucket-level actions like s3:ListBucket
          "arn:aws:s3:::wfprev-${var.SHORTENED_ENV}-site/*",      # Object-level actions
          "arn:aws:cloudfront::${var.TARGET_AWS_ACCOUNT_ID}:distribution/*" # CloudFront distrbution
        ]
      }
    ]
  })
}

data "aws_iam_openid_connect_provider" "github_openid_connect_provider" {
  url = "https://token.actions.githubusercontent.com"
}

resource "aws_iam_role_policy_attachment" "github_actions_policy_attach" {
  role       = aws_iam_role.github_actions_role.name
  policy_arn = aws_iam_policy.github_actions_policy.arn
}

resource "aws_iam_policy" "invoke_lambda" {
  name = "wfprev-invoke-lambda"
  description = "Allow invoking the GDB Processor Lambda"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = "lambda:InvokeFunction",
        Resource = aws_lambda_function.gdb_processor.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "attach_invoke_lambda" {
  role       = aws_iam_role.wfprev_app_container_role.name
  policy_arn = aws_iam_policy.invoke_lambda.arn
}

# Output for the AWS Account ID
output "github_actions_account_id" {
  value       = regex("^arn:aws:iam::([0-9]+):", aws_iam_role.github_actions_role.arn)[0]
  description = "AWS Account ID associated with the GitHub Actions role."
}

# Output for the Role Name
output "github_actions_role_name" {
  value       = regex(":role/([^:]+)$", aws_iam_role.github_actions_role.arn)[0]
  description = "Name of the GitHub Actions role."
}
