# ---------------------------------------------------------------------------------------------------------------------
# Parameter Store
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_ssm_parameter" "secrets_parameter" {
  name        = "/${var.environment}/${var.project_name}/secrets/hmac-secrets"
  type        = "SecureString"
  description = "Secrets used for authentication with External data provider"
  tier        = "Standard"
  value       = jsonencode({
    secret_1 = {
      secret = random_bytes.auth_secret_1.base64
      createdAt = timestamp()
    }
    secret_2 = {
      secret = random_bytes.auth_secret_2.base64
      createdAt = timeadd(timestamp(), "1m") # Doing this so there is always an older secret
    }
  })

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_iam_policy" "parameter_store_secrets_policy" {
  name        = "${var.project_name}-hmac-secret-${var.environment}"
  description = "IAM policy for accessing the HMAC 512 bits parameter store secret"

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow"
        "Action" : [
          "ssm:GetParameter",
        ],
        "Resource" : aws_ssm_parameter.secrets_parameter.arn,
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