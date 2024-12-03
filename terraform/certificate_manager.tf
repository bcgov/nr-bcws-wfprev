//NOTE: US certificate is needed for cloudfront

resource "aws_acm_certificate" "wfprev_domain_certificate" {
  domain_name       = data.aws_route53_zone.wfprev_route53_zone.name
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }

  provider = aws.aws-us
}

resource "aws_acm_certificate_validation" "domain_certificate_validation" {
  certificate_arn         = aws_acm_certificate.wfprev_domain_certificate.arn
  validation_record_fqdns = [for record in aws_route53_record.cert_validation_record : record.fqdn]

  provider = aws.aws-us
}