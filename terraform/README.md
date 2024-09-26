alb.tf:
The alb.tf file configures an Application Load Balancer (ALB) to route traffic to the WFPREV API and UI. It creates a secure internal ALB with specified security groups and subnets. The file also sets up listeners on the ALB to handle HTTP requests and define fixed responses for unmatched routes. It configures target groups for both the API and UI, specifying health checks and routing traffic based on health status. Additionally, the file includes an example SSL certificate for testing purposes, with a note to replace it with a real certificate in a production environment.

api_gateway.tf: 
The api_gateway.tf file configures an API Gateway to expose the WFPREV API for external access. It sets up a secure connection between the API Gateway and the internal VPC resources using a VPC link, and routes incoming HTTP requests to a load balancer within the VPC. The file also defines deployment stages and automates redeployment when changes are made, ensuring a consistent and reliable API interface across different environments.

ecs.tf:
This file sets up an ECS cluster for deploying the WFPREV server and client applications using FARGATE and FARGATE_SPOT capacity providers. It defines task definitions for both server and client containers, including resource allocations, environment variables, and logging configurations. The file also creates ECS services to manage these tasks, linking them to the appropriate load balancers for traffic routing and health checks.

iam.tf:
This file defines IAM roles and policies to grant necessary permissions for ECS tasks and Lambda functions. It configures roles for ECS and Lambda execution, attaches relevant AWS policies for accessing resources like SQS, VPC, and RDS, and includes custom policies for specific tasks like CloudFront invalidation.

main.tf:
The main.tf file sets up the Terraform configuration, specifying the required AWS provider version (~> 4.0) and ensuring Terraform itself is version 1.1.0 or higher. This establishes the foundation for managing AWS resources within the project

secrets.tf:
The secrets.tf file manages GitHub credentials in AWS Secrets Manager. It creates a secret named bcws_wfprev_creds_${var.TARGET_ENV} and stores the GitHub username and token in a secret version for secure access and management.

terragrunt-deploy.yml:
The terragrunt-deploy.yml GitHub Actions workflow automates the deployment process for the WFPREV application using Terragrunt. It supports multiple environments (dev, test, prod) and various configuration options, such as image tags and schema names. The workflow sets up AWS credentials, Terraform, and Terragrunt, then applies the Terragrunt configurations to deploy the application infrastructure and services. It also determines which Liquibase command to run for database changes and manages environment-specific variables for the deployment process.