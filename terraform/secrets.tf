resource "aws_secretsmanager_secret" "githubCredentials" {
  name = "bcws_wfprev_creds_${var.TARGET_ENV}"
}
