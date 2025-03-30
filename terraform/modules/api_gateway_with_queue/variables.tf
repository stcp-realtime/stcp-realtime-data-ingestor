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
# Authorizer Lambda
# ---------------------------------------------------------------------------------------------------------------------

variable "authorizer_function_arn" {
  description = "Authorizer Lambda ARN"
  type = string
}

variable "authorizer_function_invoke_arn" {
  description = "Authorizer Lambda Invoke ARN"
  type = string
}
