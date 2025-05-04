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

variable "lambda_function_source_path" {
  description = "Path to the secrets-rotator function zip"
  type        = string
  default     = "../../../authorizer/target/function.zip"
}

variable "lambda_runtime" {
  description = "Runtime of the secrets-rotator lambda function"
  type        = string
}

variable "lambda_log_level" {
  description = "Log level of the secrets-rotator lambda"
  type        = string
}
