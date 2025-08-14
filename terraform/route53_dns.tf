data "aws_route53_zone" "wfprev_route53_zone" {
  name         = "${var.client_name}.${var.SHORTENED_ENV}.${var.gov_domain}"
}

resource "aws_route53_record" "cert_validation_record" {
  for_each = {
    for dvo in aws_acm_certificate.wfprev_domain_certificate.domain_validation_options : dvo.domain_name => {
      name = dvo.resource_record_name
      record = dvo.resource_record_value
      type = dvo.resource_record_type
    }
  }
  name            = each.value.name
  records         = [each.value.record]
  type            = each.value.type
  zone_id = data.aws_route53_zone.wfprev_route53_zone.zone_id
  allow_overwrite = true
  ttl = 300
}

// ca-central-1 cert validation for ALB listener
resource "aws_route53_record" "cert_validation_record_ca" {
  for_each = {
    for dvo in aws_acm_certificate.wfprev_domain_certificate_ca.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  name    = each.value.name
  type    = each.value.type
  records = [each.value.record]
  ttl     = 300
  zone_id = data.aws_route53_zone.wfprev_route53_zone.zone_id
  allow_overwrite = true
}

resource "aws_route53_record" "wfprev_vanity_url" {
  zone_id = data.aws_route53_zone.wfprev_route53_zone.id
  name    = "${var.client_name}.${var.SHORTENED_ENV}.${var.gov_domain}"
  type    = "A"
  alias {
    name                   = aws_cloudfront_distribution.wfprev_app_distribution.domain_name
    zone_id                = aws_cloudfront_distribution.wfprev_app_distribution.hosted_zone_id
    evaluate_target_health = true
  }
}
