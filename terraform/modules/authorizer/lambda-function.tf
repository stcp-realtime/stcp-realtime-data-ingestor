# ---------------------------------------------------------------------------------------------------------------------
# Lambda Function
# ---------------------------------------------------------------------------------------------------------------------

locals {
  authorizer_lambda_name = "${var.project_name}-authorizer-${var.environment}"
}

resource "aws_lambda_function" "authorizer" {
  function_name = local.authorizer_lambda_name
  role          = aws_iam_role.lambda_role.arn
  filename      = var.authorizer_function_source_path
  source_code_hash = filebase64sha256(var.authorizer_function_source_path)
  memory_size   = 128
  handler       = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
  runtime       = var.authorizer_lambda_runtime
  timeout       = 10

  environment {
    variables = {
      SECRETS_PARAMETER_DIRECTORY_PATH = local.secrets_parameter_directory_path
    }
  }

  depends_on = [
    aws_iam_role_policy_attachment.authorizer_logs,
    aws_cloudwatch_log_group.authorizer
  ]
}

data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "lambda_role" {
  name               = local.authorizer_lambda_name
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

resource "aws_iam_role_policy_attachment" "parameter-store-secret" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.parameter_store_secrets_policy.arn
}

resource "aws_iam_role_policy_attachment" "authorizer_logs" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.authorizer_logs.arn
}
