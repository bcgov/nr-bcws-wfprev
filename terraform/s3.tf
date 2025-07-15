
module "s3_secure_bucket" {
  source = "git::https://github.com/bcgov/quickstart-aws-helpers.git//terraform/modules/s3-secure-bucket?ref=v0.0.5"
  
  bucket_name = "wfprev-${var.SHORTENED_ENV}-site"
  force_destroy = true

  # S3 Bucket Policy for public access
  bucket_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          "AWS" : "${aws_cloudfront_origin_access_identity.oai.iam_arn}"
        },
        Action   = "s3:GetObject",
        Resource = "arn:aws:s3:::wfprev-${var.SHORTENED_ENV}-site/*"
      },
      {
        Effect = "Allow",
        Principal = {
          "AWS" : "arn:aws:iam::${var.TARGET_AWS_ACCOUNT_ID}:role/github-actions-role"
        },
        Action = [
          "s3:ListBucket",
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject"
        ],
        Resource = [
          "arn:aws:s3:::wfprev-${var.SHORTENED_ENV}-site",
          "arn:aws:s3:::wfprev-${var.SHORTENED_ENV}-site/*"
        ]
      }
    ]
  })
}

resource "aws_s3_bucket" "alb_logs" {
  bucket = "wfprev-${var.SHORTENED_ENV}-alb-logs-bucket"

  force_destroy = true

  tags = {
    Environment = var.TARGET_ENV
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "alb_logs_lifecycle" {
  bucket = aws_s3_bucket.alb_logs.id

  rule {
    id     = "expire-alb-logs"
    status = "Enabled"

    filter {}  # Empty filter means apply to all objects

    expiration {
      days = 90
    }

    noncurrent_version_expiration {
      noncurrent_days = 30
    }
  }
}

# Uploading assets. This shouldn't be needed because we'll push them up from the 
# github action, vs having terraform fetch them
#resource "aws_s3_object" "upload-assets" {
#  for_each = fileset("${var.web-assets-path}", "**/*")
#  bucket = module.s3_secure_bucket.bucket
#  key = each.value
#  source = "${var.web-assets-path}/${each.value}"
#  content_type = lookup(var.mime_types, regex("\\.[^.]+$", each.value), "application/octet-stream")
#}

resource "aws_s3_bucket_policy" "alb_logs_policy" {
  bucket = aws_s3_bucket.alb_logs.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid    = "AWSALBLoggingPermissions"
        Effect = "Allow"
        Principal = {
          Service = [
            "logdelivery.elasticloadbalancing.amazonaws.com",
            "elasticloadbalancing.amazonaws.com"
          ],
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        }
        Action = "s3:PutObject"
        Resource = "${aws_s3_bucket.alb_logs.arn}/*"
      }
    ]
  })
}

data "aws_caller_identity" "current" {}

output "s3_bucket_name" {
  value = module.s3_secure_bucket.bucket_name
}