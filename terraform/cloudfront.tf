# CloudFront Origin Access Identity (OAI) for secure access to S3
resource "aws_cloudfront_origin_access_identity" "oai" {
  comment = "OAI for wfprev UI"
}

# CloudFront Distribution
resource "aws_cloudfront_distribution" "wfprev_app_distribution" {
  origin {
    domain_name = aws_s3_bucket.wfprev_site_bucket.bucket_regional_domain_name
    origin_id   = "S3-${aws_s3_bucket.wfprev_site_bucket.id}"

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.oai.cloudfront_access_identity_path
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"

  # Configure cache behaviors
  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "S3-${aws_s3_bucket.wfprev_site_bucket.id}"
    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    min_ttl     = 0
    default_ttl = 86400
    max_ttl     = 31536000
  }

  # Viewer Certificate
  viewer_certificate {
    cloudfront_default_certificate = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }
}

output "cloudfront_distribution_id" {
  value = aws_cloudfront_distribution.wfprev_app_distribution.id
}

