# ---------------------------------------------------------------------------------------------------------------------
# API Gateway - Lambda IAM
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_iam_role" "lambda_invocation_role" {
  name               = "${var.project_name}-authorizer-invocation-${var.environment}"
  assume_role_policy = data.aws_iam_policy_document.api_gateway_assume_role.json
}

resource "aws_iam_role_policy_attachment" "lambda_invocation_policy" {
  policy_arn = aws_iam_policy.lambda_invocation_policy.arn
  role       = aws_iam_role.lambda_invocation_role.name
}

resource "aws_iam_policy" "lambda_invocation_policy" {
  name_prefix = "${var.project_name}-authorizer-invocation-policy-${var.environment}"
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Action" : "lambda:InvokeFunction",
        "Effect" : "Allow",
        "Resource" : var.authorizer_function_arn
      }
    ]
  })
}

# ---------------------------------------------------------------------------------------------------------------------
# API Gateway - SQS IAM
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_iam_role" "sqs_invocation_role" {
  name               = "${var.project_name}-sqs-invocation-${var.environment}"
  assume_role_policy = data.aws_iam_policy_document.api_gateway_assume_role.json
}

resource "aws_iam_role_policy_attachment" "sqs_invocation_policy" {
  policy_arn = aws_iam_policy.sqs_invocation_policy.arn
  role       = aws_iam_role.sqs_invocation_role.name
}

resource "aws_iam_policy" "sqs_invocation_policy" {
  name_prefix = "${var.project_name}-sqs-invocation-policy-${var.environment}"
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Action" : "sqs:SendMessage",
        "Effect" : "Allow",
        "Resource" : aws_sqs_queue.sqs_bus.arn
      }
    ]
  })
}

data "aws_iam_policy_document" "api_gateway_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type = "Service"
      identifiers = ["apigateway.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}