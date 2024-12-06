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

  origin {
    domain_name = trimprefix(aws_apigatewayv2_api.wfprev_api_gateway.api_endpoint, "https://")
    origin_id = "wfprev-api-origin"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "https-only"
      origin_ssl_protocols = [
      "TLSv1.2"]
    }
  }

  enabled             = true
  is_ipv6_enabled     = true

  aliases = [ "wfprev-${var.TARGET_ENV}.${var.gov_domain}" ]

  # Configure cache behaviors
  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "S3-${aws_s3_bucket.wfprev_site_bucket.id}"
    viewer_protocol_policy = "redirect-to-https"

    //Rewrite requests to always hit /index.html
    function_association {
			event_type   = "viewer-request"
			function_arn = aws_cloudfront_function.rewrite_uri.arn
		}

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



    ordered_cache_behavior {
    path_pattern     = "/${aws_apigatewayv2_stage.wfprev_stage.name}*"
    allowed_methods = [
      "DELETE",
      "GET",
      "HEAD",
      "OPTIONS",
      "PATCH",
      "POST",
      "PUT"
    ]
    cached_methods   = ["GET", "HEAD", "OPTIONS"]
    target_origin_id = "wfprev-api-origin"

    forwarded_values {
      query_string = true
      headers      = ["Origin", "Authorization"]

      cookies {
        forward = "none"
      }
    }

    min_ttl                = 0
    default_ttl            = 0 //Not caching yet, just confirming cloudfront works
    max_ttl                = 0
    compress               = true
    viewer_protocol_policy = "redirect-to-https"
  }


  viewer_certificate {
    acm_certificate_arn = aws_acm_certificate.wfprev_domain_certificate.arn
    ssl_support_method  = "sni-only"
  }

  price_class = "PriceClass_100"

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }
}

resource "aws_cloudfront_function" "rewrite_uri" {
	name    = "rewrite-request-wfprev-${var.TARGET_ENV}"
	runtime = "cloudfront-js-1.0"
	code    = <<EOF
  function handler(event) {
    var request = event.request;
    if (!String(request.uri).match(/\.[a-z]+/)) {
      request.uri = request.uri.replace(/.*/, "/index.html");
    }
    return request;
  }
EOF
}

output "cloudfront_distribution_id" {
  value = aws_cloudfront_distribution.wfprev_app_distribution.id
}