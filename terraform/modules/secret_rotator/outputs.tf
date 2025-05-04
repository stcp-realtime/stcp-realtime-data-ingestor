# ---------------------------------------------------------------------------------------------------------------------
# Outputs
# ---------------------------------------------------------------------------------------------------------------------

output "secret_parameter_arns" {
  description = "Parameter ARNs of the secrets"
  value = [aws_ssm_parameter.secrets_parameter_1.arn, aws_ssm_parameter.secrets_parameter_2.arn]
}
