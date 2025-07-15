module "networking" {
  source = "git::https://github.com/bcgov/quickstart-aws-helpers.git//terraform/modules/networking?ref=v0.0.5"
  
  target_env = var.NAMESPACE_ENV
}