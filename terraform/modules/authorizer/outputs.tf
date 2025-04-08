# ---------------------------------------------------------------------------------------------------------------------
# Outputs
# ---------------------------------------------------------------------------------------------------------------------

output "authorizer_function_arn" {
  description = "Authorizer Lambda Function's ARN"
  value = aws_lambda_function.authorizer.arn
}

output "authorizer_function_invoke_arn" {
  description = "Authorizer Lambda Function's Invoke ARN"
  value = aws_lambda_function.authorizer.invoke_arn
}
