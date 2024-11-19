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

# Retrieve the parameter value from AWS Systems Manager (SSM) Parameter Store
# This fetches the SecureString parameter containing the GitHub Actions user's keys.
# The parameter's value will be used later for accessing AWS resources securely.
# data "aws_ssm_parameter" "github_actions_user_keys" {
#   name = "/iam_users/wfprev_github_actions_user_keys"
# }

# Create an IAM user specifically for GitHub Actions
# This user will be granted limited permissions for performing specific tasks (e.g., S3 operations and CloudFront invalidations).
resource "aws_iam_user" "github_actions_user" {
  name = "wfprev_github_actions_user"
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
        Resource = "arn:aws:ssm:ca-central-1:183631341627:parameter/iam_users/wfprev_github_actions_user_keys"
      }
    ]
  })
}

# Attach the SSM parameter access policy to the GitHub Actions IAM user
# This links the user with the necessary permissions to read the SSM parameter securely.
resource "aws_iam_user_policy_attachment" "ssm_parameter_access_attachment" {
  user       = data.aws_iam_user.wfprev_github_actions_user.name
  policy_arn = aws_iam_policy.ssm_parameter_access.arn
}

# Define an IAM policy for GitHub Actions user to perform specific operations
# This policy grants permissions to:
# - Upload/delete objects in an S3 bucket
# - Invalidate cached content in CloudFront
resource "aws_iam_user_policy" "github_actions_policy" {
  name = "github-actions-policy"
  user = data.aws_iam_user.wfprev_github_actions_user.name

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = ["s3:PutObject", "s3:DeleteObject"],
        Resource = "${aws_s3_bucket.wfprev_site_bucket.arn}/*"
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
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Federated = "arn:aws:iam::$${var.TARGET_AWS_ACCOUNT_ID}:oidc-provider/token.actions.githubusercontent.com"
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:sub" = "repo:bcgov/nr-bcws-wfprev:ref:refs/heads/main"
          }
        }
      }
    ]
  })
}

