# ---------------------------------------------------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------------------------------------------------

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.91.0"
    }
  }
  backend "s3" {
    workspace_key_prefix = "stcp-realtime-data-ingestor"
    bucket               = "stcp-realtime-tfstate-dev"
    key                  = "stcp-realtime-data-ingestor.tfstate"
    region               = "eu-south-2"
    use_lockfile         = true
  }
}

provider "aws" {
  region = local.aws_region

  default_tags {
    tags = {
      environment = local.environment
      project     = local.project_name
    }
  }
}

locals {
  project_name = "stcp-realtime-data-ingestor"
  environment  = "dev"
  aws_region   = "eu-south-2"
}

module "authorizer" {
  source                    = "../../modules/authorizer"
  project_name              = local.project_name
  environment               = local.environment
  aws_region                = local.aws_region
  authorizer_lambda_runtime = var.authorizer_lambda_runtime
}

module "api_gateway_with_queue" {
  source                         = "../../modules/api_gateway_with_queue"
  project_name                   = local.project_name
  environment                    = local.environment
  aws_region                     = local.aws_region
  authorizer_function_arn        = module.authorizer.authorizer_function_arn
  authorizer_function_invoke_arn = module.authorizer.authorizer_function_invoke_arn
}

