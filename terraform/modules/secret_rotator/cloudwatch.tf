# ---------------------------------------------------------------------------------------------------------------------
# Cloudwatch Logging
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_cloudwatch_log_group" "secret_rotator" {
  name              = "/aws/lambda/${local.secret_rotator_lambda_name}"
  retention_in_days = 1
  log_group_class   = "STANDARD"
}

resource "aws_iam_policy" "secret_rotator_logs" {
  name        = "${local.secret_rotator_lambda_name}-logs"
  description = "IAM policy for logging from authorizer lambda"

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Action" : [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        "Resource" : "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:${aws_cloudwatch_log_group.secret_rotator.name}:*",
        "Effect" : "Allow"
      }
    ]
  })
}
