# ---------------------------------------------------------------------------------------------------------------------
# Parameter Store
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_ssm_parameter" "hmac_secret_parameter" {
  name        = "/${var.environment}/${var.project_name}/secrets/hmac-secrets"
  type        = "SecureString"
  description = "Secrets used for authentication with External data provider"
  tier        = "Standard"
  value       = jsonencode({
    key_1 = random_bytes.hmac_sha3_512_key_1.base64
    key_2 = random_bytes.hmac_sha3_512_key_2.base64
  })

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_iam_policy" "parameter-store-hmac-secret" {
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
        "Resource" : aws_ssm_parameter.hmac_secret_parameter.arn,
      }
    ]
  })
}

resource "random_bytes" "hmac_sha3_512_key_1" {
  length = 64
}

resource "random_bytes" "hmac_sha3_512_key_2" {
  length = 64
}