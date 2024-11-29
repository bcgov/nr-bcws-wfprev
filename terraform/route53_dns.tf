data "aws_route53_zone" "wfprev_route53_zone" {
  name         = "wfprev-${var.TARGET_ENV}.${var.gov_domain}"
}

resource "aws_route53_record" "cert_validation_record" {
  zone_id = data.aws_route53_zone.wfprev_route53_zone.zone_id
  name = aws_acm_certificate.domain_certificate.resource_record_name
  records = [aws_acm_certificate.domain_certificate.resource_record_value]
  type = aws_acm_certificate.domain_certificate.resource_record_type
  allow_overwrite = true
  ttl = 300
}
