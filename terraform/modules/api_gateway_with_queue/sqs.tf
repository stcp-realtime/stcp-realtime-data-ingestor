# ---------------------------------------------------------------------------------------------------------------------
# SQS
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_sqs_queue" "sqs_bus_dlq" {
  name                      = "${var.project_name}-bus-events-dlq-${var.environment}"
  sqs_managed_sse_enabled   = true
  message_retention_seconds = 604800 // 7 days
}

resource "aws_sqs_queue" "sqs_bus" {
  name                      = "${var.project_name}-bus-events-${var.environment}"
  policy                    = data.aws_iam_policy_document.api_gateway_send_message.json
  receive_wait_time_seconds = 20
  message_retention_seconds = 300 // 5 minutes
  sqs_managed_sse_enabled   = true

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.sqs_bus_dlq.arn
    maxReceiveCount     = 2
  })
}

data "aws_iam_policy_document" "api_gateway_send_message" {
  version = "2012-10-17"
  statement {
    effect = "Allow"

    principals {
      type = "Service"
      identifiers = ["apigateway.amazonaws.com"]
    }

    actions = ["sqs:SendMessage"]
    resources = [
      "arn:aws:sqs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:${var.project_name}-bus-events-${var.environment}"
    ]

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values = [aws_apigatewayv2_api.data_ingestor.arn]
    }
  }
}
