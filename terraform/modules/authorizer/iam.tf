# ---------------------------------------------------------------------------------------------------------------------
# Identity and Access Management (IAM)
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_iam_policy" "parameter_store_secrets_policy" {
  name        = "${var.project_name}-secrets-${var.environment}"
  description = "IAM policy for accessing the 512 bits parameter store secrets"

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow"
        "Action" : [
          "ssm:GetParametersByPath",
        ],
        "Resource" : "arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter${var.secrets_directory_path}/*",
      }
    ]
  })
}
