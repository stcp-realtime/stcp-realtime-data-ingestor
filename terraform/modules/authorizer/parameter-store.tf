# ---------------------------------------------------------------------------------------------------------------------
# Parameter Store
# ---------------------------------------------------------------------------------------------------------------------

locals {
  secrets_parameter_directory_path = "/${var.environment}/${var.project_name}/secrets"
}

resource "aws_ssm_parameter" "secrets_parameter_1" {
  name        = "${local.secrets_parameter_directory_path}/secret_1"
  type        = "SecureString"
  description = "Secrets used for authentication with External data provider"
  tier        = "Standard"
  value = random_bytes.auth_secret_1.base64

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "secrets_parameter_2" {
  name        = "${local.secrets_parameter_directory_path}/secret_2"
  type        = "SecureString"
  description = "Secrets used for authentication with External data provider"
  tier        = "Standard"
  value       = random_bytes.auth_secret_2.base64

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_iam_policy" "parameter_store_secrets_policy" {
  name        = "${var.project_name}-secrets-${var.environment}"
  description = "IAM policy for accessing the 512 bits parameter store secrets"

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow"
        "Action" : [
          "ssm:GetParameter",
        ],
        "Resource" : "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter${local.secrets_parameter_directory_path}/*",
      }
    ]
  })
}

resource "random_bytes" "auth_secret_1" {
  length = 64
}

resource "random_bytes" "auth_secret_2" {
  length = 64
}