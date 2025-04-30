# ---------------------------------------------------------------------------------------------------------------------
# General
# ---------------------------------------------------------------------------------------------------------------------

variable "project_name" {
  description = "Name of the application."
  type        = string
}

variable "environment" {
  description = "Workspace to distinguish resources within CI environment. Equals var.stage on other environments!"
  type        = string
}

variable "aws_region" {
  description = "AWS Region to use for resources."
  type        = string
}

# ---------------------------------------------------------------------------------------------------------------------
# AWS Lambda
# ---------------------------------------------------------------------------------------------------------------------

variable "secret_rotator_function_source_path" {
  description = "Path to the authorizer function zip"
  type        = string
  default     = "../../../authorizer/target/function.zip"
}

variable "secret_rotator_lambda_runtime" {
  description = "Runtime of the authorizer lambda function"
  type        = string
}
