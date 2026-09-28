
module "s3_secure_bucket" {
  source = "git::https://github.com/bcgov/quickstart-aws-helpers.git//terraform/modules/s3-secure-bucket?ref=v0.0.5"
  
  bucket_name = "wfprev-${var.TFC_PROJECT}-${var.SHORTENED_ENV}-site"
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
        Resource = "arn:aws:s3:::wfprev-${var.TFC_PROJECT}-${var.SHORTENED_ENV}-site/*"
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
          "arn:aws:s3:::wfprev-${var.TFC_PROJECT}-${var.SHORTENED_ENV}-site",
          "arn:aws:s3:::wfprev-${var.TFC_PROJECT}-${var.SHORTENED_ENV}-site/*"
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

# Report exports: the API and the report Lambda write finished report files here, and
# browsers download them through short-lived presigned URLs. Nothing is public.
resource "aws_s3_bucket" "report_exports" {
  bucket = "wfprev-${var.TFC_PROJECT}-${var.SHORTENED_ENV}-report-exports"

  force_destroy = true

  tags = {
    Environment = var.TARGET_ENV
  }
}

resource "aws_s3_bucket_public_access_block" "report_exports" {
  bucket = aws_s3_bucket.report_exports.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "report_exports" {
  bucket = aws_s3_bucket.report_exports.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "report_exports" {
  bucket = aws_s3_bucket.report_exports.id

  # Exported files are kept for 24 hours; the download tray shows older exports as expired.
  rule {
    id     = "expire-report-exports"
    status = "Enabled"

    filter {
      prefix = "jobs/"
    }

    expiration {
      days = 1
    }
  }

  # Parts left behind when an API task dies in the middle of a streamed upload.
  rule {
    id     = "abort-incomplete-report-uploads"
    status = "Enabled"

    filter {
      prefix = "jobs/"
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }
}

resource "aws_s3_bucket_policy" "report_exports" {
  bucket = aws_s3_bucket.report_exports.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid       = "DenyInsecureTransport"
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource = [
          aws_s3_bucket.report_exports.arn,
          "${aws_s3_bucket.report_exports.arn}/*"
        ]
        Condition = {
          Bool = { "aws:SecureTransport" = "false" }
        }
      }
    ]
  })

  depends_on = [aws_s3_bucket_public_access_block.report_exports]
}

module s3_cloudfront_logs {
  source = "git::https://github.com/bcgov/quickstart-aws-helpers.git//terraform/modules/s3-cloudfront-logs?ref=v0.0.5"

  bucket_name = "wfprev-${var.SHORTENED_ENV}-cloudfront-logs"
  account_id = data.aws_caller_identity.current.account_id
}

output "s3_bucket_name" {
  value = module.s3_secure_bucket.bucket_name
}

output "report_exports_bucket_name" {
  value = aws_s3_bucket.report_exports.bucket
}