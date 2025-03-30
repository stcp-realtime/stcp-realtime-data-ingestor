# ---------------------------------------------------------------------------------------------------------------------
# Parameter Store
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_ssm_parameter" "hmac_secret_parameter" {
  // TODO: Need to thing about doing it like this, will it fail on deploy?
  name        = "/${var.environment}/${var.project_name}/secrets/hmac-secret"
  type        = "SecureString"
  description = "Secret used for authentication with External data provider"
  tier        = "Standard"
  value       = random_bytes.hmac_sha3_512_key.base64
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

resource "random_bytes" "hmac_sha3_512_key" {
  length = 64
}
