# Bucket create. Public-read or private?
resource "aws_s3_bucket" "wfprev_site_bucket" {
  bucket        = "wfprev-${var.TARGET_ENV}-site"
  force_destroy = true

  website {
    index_document = "index.html"
    error_document = "index.html"
  }
}

resource "aws_cloudfront_origin_access_identity" "oai" {
  comment = "OAI for WFPREV site."
}

resource "aws_cloudfront_distribution" "s3_distribution" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "Distribution for WFPREV site."
  default_root_object = "index.html"

  origin {
    domain_name = aws_s3_bucket.wfprev_site_bucket.bucket_regional_domain_name
    origin_id   = aws_s3_bucket.wfprev_site_bucket.bucket

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.oai.cloudfront_access_identity_path
    }
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = aws_s3_bucket.wfprev_site_bucket.bucket

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = {
    Name = "wfprev-distribution"
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
        Resource = "${aws_s3_bucket.wfprev_site_bucket.arn}/*"
      }
    ]
  })
}

output "s3_bucket_name" {
  value = aws_s3_bucket.wfprev_site_bucket.bucket
}
