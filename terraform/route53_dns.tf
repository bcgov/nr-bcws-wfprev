data "aws_route53_zone" "wfprev_route53_zone" {
  name         = "wfprev-${var.TARGET_ENV}.${var.gov_domain}"
}

resource "aws_route53_record" "cert_validation_record" {
  zone_id = data.aws_route53_zone.wfprev_route53_zone.zone_id
  name = aws_acm_certificate.wfprev_domain_certificate.resource_record_name
  records = [aws_acm_certificate.wfprev_domain_certificate.resource_record_value]
  type = aws_acm_certificate.wfprev_domain_certificate.resource_record_type
  allow_overwrite = true
  ttl = 300
}

resource "aws_route53_record" "wfprev_vanity_url" {
  zone_id = data.aws_route53_zone.wfprev_route53_zone.id
  name    = "wfprev-${var.TARGET_ENV}.${var.gov_domain}"
  type    = "A"
  alias {
    name                   = aws_cloudfront_distribution.wfprev_app_distribution.domain_name
    zone_id                = aws_cloudfront_distribution.wfprev_app_distribution.hosted_zone_id
    evaluate_target_health = true
  }
}
