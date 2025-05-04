# ---------------------------------------------------------------------------------------------------------------------
# Event Bridge
# ---------------------------------------------------------------------------------------------------------------------

locals {
  secret_rotator_schedule_name = "${var.project_name}-secret-rotator-${var.environment}"
}

resource "aws_scheduler_schedule_group" "secret_rotator_schedule_group" {
  name = "${var.project_name}-secret-rotator-group-${var.environment}"
}

resource "aws_scheduler_schedule" "secret_rotator_schedule" {
  name       = local.secret_rotator_schedule_name
  group_name = aws_scheduler_schedule_group.secret_rotator_schedule_group.id

  target {
    arn      = aws_lambda_function.secret_rotator.arn
    role_arn = aws_iam_role.schedule_role.arn
  }

  schedule_expression          = "rate(3 hours)" // TODO: Check time
  schedule_expression_timezone = "UTC"

  flexible_time_window {
    mode = "OFF"
  }

  depends_on = [
    aws_iam_role_policy.scheduler_invoke_lambda_policy
  ]
}

data "aws_iam_policy_document" "schedule_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["scheduler.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "schedule_role" {
  name               = local.secret_rotator_schedule_name
  assume_role_policy = data.aws_iam_policy_document.schedule_assume_role.json
}

resource "aws_iam_role_policy" "scheduler_invoke_lambda_policy" {
  name = "${local.secret_rotator_schedule_name}-invoke"
  role = aws_iam_role.schedule_role.id

  policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = "lambda:InvokeFunction"
        Resource = aws_lambda_function.secret_rotator.arn
      }
    ]
  })
}
