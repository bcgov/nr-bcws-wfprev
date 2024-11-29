resource "aws_acm_certificate" "domain_certificate" {
  domain_name       = data.aws_route53_zone.wfprev_route53_zone.name
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_acm_certificate_validation" "domain_certificate_validation" {
  certificate_arn         = aws_acm_certificate.domain_certificate.arn
  validation_record_fqdns = aws_route53_record.cert_validation_record.fqdn
}