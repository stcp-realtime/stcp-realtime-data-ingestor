# ---------------------------------------------------------------------------------------------------------------------
# Lambda Function
# ---------------------------------------------------------------------------------------------------------------------

locals {
  secret_rotator_lambda_name = "${var.project_name}-secret-rotator-${var.environment}"
}

resource "aws_lambda_function" "secret_rotator" {
  function_name    = local.secret_rotator_lambda_name
  role             = aws_iam_role.lambda_role.arn
  filename         = var.lambda_function_source_path
  source_code_hash = filebase64sha256(var.lambda_function_source_path)
  memory_size      = 128
  handler          = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
  runtime          = var.lambda_runtime
  timeout          = 10

  environment {
    variables = {
      LOG_LEVEL = var.lambda_log_level
    }
  }

  depends_on = [
    aws_iam_role_policy_attachment.secret_rotator_logs,
    aws_cloudwatch_log_group.secret_rotator
  ]
}

data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "lambda_role" {
  name               = local.secret_rotator_lambda_name
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
}

resource "aws_iam_role_policy_attachment" "parameter-store-secret" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.parameter_store_secrets_policy.arn
}

resource "aws_iam_role_policy_attachment" "secret_rotator_logs" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.secret_rotator_logs.arn
}
