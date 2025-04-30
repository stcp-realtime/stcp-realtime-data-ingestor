# ---------------------------------------------------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------------------------------------------------

terraform {
  required_providers {
    random = {
      source = "hashicorp/random"
      version = "3.7.1"
    }
  }
}

data "aws_caller_identity" "current" {}

locals {
  lambda_resource_limits = {
    "java21" = {
      "memory_size" = 256
      "timeout"     = 40
    }

    "provided.al2023" = {
      "memory_size" = 128
      "timeout"     = 10
    }
  }
}
