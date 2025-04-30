# ---------------------------------------------------------------------------------------------------------------------
# Outputs
# ---------------------------------------------------------------------------------------------------------------------

output "secrets_directory_path" {
  description = "Parent directory of the secrets"
  value = local.secrets_parameter_directory_path
}
