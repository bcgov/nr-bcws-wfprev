# Bucket create. Public-read or private?
resource "aws_s3_bucket" "wfprev_site_bucket" {
  bucket        = "wfprev-${var.TARGET_ENV}-site"
  force_destroy = true

  website {
    index_document = "index.html"
    error_document = "index.html"
  }

  lifecycle {
    ignore_changes = [bucket]
  }
}

resource "aws_s3_bucket" "alb_logs" {
  bucket = "wfprev-${var.TARGET_ENV}-alb-logs-bucket"

  force_destroy = true

  tags = {
    Environment = var.TARGET_ENV
  }
}

# Uploading assets. This shouldn't be needed because we'll push them up from the 
# github action, vs having terraform fetch them
#resource "aws_s3_object" "upload-assets" {
#  for_each = fileset("${var.web-assets-path}", "**/*")
#  bucket = aws_s3_bucket.wfprev_site_bucket.bucket
#  key = each.value
#  source = "${var.web-assets-path}/${each.value}"
#  content_type = lookup(var.mime_types, regex("\\.[^.]+$", each.value), "application/octet-stream")
#}

# S3 Bucket Policy for public access
resource "aws_s3_bucket_policy" "wfprev_site_bucket_policy" {
  bucket = aws_s3_bucket.wfprev_site_bucket.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          "AWS" : "${aws_cloudfront_origin_access_identity.oai.iam_arn}"
        },
        Action   = "s3:GetObject",
        Resource = "arn:aws:s3:::wfprev-${var.TARGET_ENV}-site/*"
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
          "arn:aws:s3:::wfprev-${var.TARGET_ENV}-site",
          "arn:aws:s3:::wfprev-${var.TARGET_ENV}-site/*"
        ]
      }
    ]
  })
}


resource "aws_s3_bucket_policy" "alb_logs_policy" {
  bucket = aws_s3_bucket.alb_logs.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid    = "AWSALBLoggingPermissions"
        Effect = "Allow"
        Principal = {
          Service = "elasticloadbalancing.amazonaws.com"
        }
        Action = "s3:PutObject"
        Resource = "${aws_s3_bucket.alb_logs.arn}/*"
      }
    ]
  })
}

resource "aws_s3_bucket_public_access_block" "alb_logs" {
  bucket                  = aws_s3_bucket.alb_logs.id
  block_public_acls        = true
  block_public_policy      = true
  ignore_public_acls       = true
  restrict_public_buckets  = true
}

output "s3_bucket_name" {
  value = aws_s3_bucket.wfprev_site_bucket.bucket
}