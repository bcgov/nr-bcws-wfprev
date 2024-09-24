resource "aws_secretsmanager_secret" "githubCredentials" {
  name = "bcws_wfprev_creds_${var.TARGET_ENV}"
}


resource "aws_secretsmanager_secret_version" "githubCredentialsCurrent" {
  secret_id     = aws_secretsmanager_secret.githubCredentials.id
  secret_string = jsonencode(
    {
      username = var.GITHUB_USERNAME
      password = var.GITHUB_TOKEN
    }
  )
}