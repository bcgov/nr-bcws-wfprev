alb.tf:
The alb.tf file configures an Application Load Balancer (ALB) to route traffic to the WFPREV API and UI. It creates a secure internal ALB with specified security groups and subnets. The file also sets up listeners on the ALB to handle HTTP requests and define fixed responses for unmatched routes. It configures target groups for both the API and UI, specifying health checks and routing traffic based on health status. Additionally, the file includes an example SSL certificate for testing purposes, with a note to replace it with a real certificate in a production environment.

api_gateway.tf: 
The api_gateway.tf file configures an API Gateway to expose the WFPREV API for external access. It sets up a secure connection between the API Gateway and the internal VPC resources using a VPC link, and routes incoming HTTP requests to a load balancer within the VPC. The file also defines deployment stages and automates redeployment when changes are made, ensuring a consistent and reliable API interface across different environments.

autoscaling.tf:
The autoscaling.tf file sets up auto scaling for multiple ECS services within the WFPrev application, including the main service, nginx, and client. It utilizes AWS Application Auto Scaling to dynamically adjust the number of tasks running in each service based on CPU utilization. The file defines scaling targets, step scaling policies for both scaling up and down, and associated CloudWatch alarms. These alarms monitor CPU utilization, triggering scale-up actions when utilization exceeds 50% and scale-down actions when it falls below 10%. The configuration allows each service to scale between 1 and 10 tasks, with a 60-second cooldown period between scaling actions. This setup ensures that the application can efficiently handle varying loads while optimizing resource usage and costs.

certificate_manager.tf:
This file requests a certificate in the us-east-1 zone, necessary for using a vanity URL with a cloudfront deployment. It also creates a validation, linking the certificate to a route53 record which is used to confirm that we have authority to create certificates for this domain

ec2.tf:
This file sets up the jumphost in EC2 - a minimal VM within the VPC but accessible via the AWS command line. By setting up port forwarding through this VM, we can access in-VPC resources with external tools such as pgadmin

ecs.tf:
This file sets up an ECS cluster for deploying the WFPREV server and client applications using FARGATE and FARGATE_SPOT capacity providers. It defines task definitions for both server and client containers, including resource allocations, environment variables, and logging configurations. The wfprev_client and wfprev_server task definitons here are setting iam roles for execution, network mode, provisionsing CPU and memory allocation, and setting volumes on the cluster for deployment of the client and server. The wfprev-liquibase task definition does the same allocation and runs database migrations. The file also creates ECS services to manage these tasks, linking them to the appropriate load balancers for traffic routing and health checks. The network condiguration sets a custom wfprev_tomcat_access security group, subnets and assigns the container a public IP address. This file also includes logConfiguration sections within the container definitions for both the WFPrev server and client task definitions. These configurations use the "awslogs" log driver, which is designed to send container logs directly to Amazon CloudWatch Logs. The options specified in each logConfiguration block determine how the logs are organized in CloudWatch. The "awslogs-create-group" option set to "true" allows ECS to automatically create the log group if it doesn't exist. The "awslogs-group" option specifies the name of the log group in CloudWatch where the logs will be sent, typically following the pattern "/ecs/{container-name}". The "awslogs-region" option ensures logs are sent to the correct AWS region, while "awslogs-stream-prefix" adds a prefix to the log stream names for easier identification. With these configurations in place, when the ECS tasks run, the Amazon ECS container agent automatically collects the stdout and stderr outputs from the containers and sends them to CloudWatch Logs without any additional setup required. This seamless integration allows for centralized logging and monitoring of ECS tasks. The CloudWatch logs can be accessed in the Log groups section of the CloudWatch service in the AWS console.

iam.tf:
This file defines IAM roles and policies to grant necessary permissions for ECS tasks and Lambda functions. It configures roles for ECS and Lambda execution, attaches relevant AWS policies for accessing resources like SQS, VPC, and RDS, and includes custom policies for specific tasks like CloudFront invalidation.

main.tf:
The main.tf file sets up the Terraform configuration, specifying the required AWS provider version (~> 4.0) and ensuring Terraform itself is version 1.1.0 or higher. This establishes the foundation for managing AWS resources within the project

network.tf:
This file imports the BCDevOps network module, which provides configuration for Terraform to use the pre-existing network infrastructure within the AWS environment

rds.tf:
This file defines a postgresql database with a master user and password. Database configuration and loading is *not* handled by terraform, instead being done by a seperate Liquibase task

route53_dns.tf:
This file defines DNS records in the wfprev-ENV namespace. Note that before terraform deployment can begin, the wfprev-ENV.nrs.gov.bc.ca must be manually created, and the details from the NS record provided to the ministry which will then enable functionality

s3.tf:
This file defines an S3 bucket filestore, which is used to host the webapp GUI. It also defines a policy which is necessary to allow users to access content stored in s3.

secrets.tf:
The secrets.tf file manages GitHub credentials in AWS Secrets Manager. It creates a secret named bcws_wfprev_creds_${var.TARGET_ENV} and stores the GitHub username and token in a secret version for secure access and management.

terragrunt-deploy.yml:
The terragrunt-deploy.yml GitHub Actions workflow automates the deployment process for the WFPREV application using Terragrunt. It supports multiple environments (dev, test, prod) and various configuration options, such as image tags and schema names. The workflow sets up AWS credentials, Terraform, and Terragrunt, then applies the Terragrunt configurations to deploy the application infrastructure and services. It also determines which Liquibase command to run for database changes and manages environment-specific variables for the deployment process.

variables.tf:
Before they can be used, variables must be declared here.