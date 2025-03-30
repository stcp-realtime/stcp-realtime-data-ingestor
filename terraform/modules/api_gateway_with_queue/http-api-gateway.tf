# ---------------------------------------------------------------------------------------------------------------------
# API Gateway
# ---------------------------------------------------------------------------------------------------------------------

resource "aws_apigatewayv2_api" "data_ingestor" {
  name          = "${var.project_name}-${var.environment}"
  protocol_type = "HTTP"

  // body = "" TODO: Maybe we want to validate the body here

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_apigatewayv2_authorizer" "authorizer" {
  api_id          = aws_apigatewayv2_api.data_ingestor.id
  authorizer_type = "REQUEST"
  name            = "${var.project_name}-${var.environment}"

  authorizer_uri = var.authorizer_function_invoke_arn
  authorizer_credentials_arn        = aws_iam_role.lambda_invocation_role.arn
  authorizer_payload_format_version = "2.0"
  authorizer_result_ttl_in_seconds  = 3600
  identity_sources = ["$request.header.x-auth-token"] // $context.domainName TODO: Use is as host?

}

resource "aws_apigatewayv2_route" "data_ingestor" {
  api_id             = aws_apigatewayv2_api.data_ingestor.id
  route_key          = "POST /notify"
  operation_name     = "Bus notifications"
  authorization_type = "CUSTOM"
  authorizer_id      = aws_apigatewayv2_authorizer.authorizer.id
  target             = "integrations/${aws_apigatewayv2_integration.sqs_integration.id}"


}

resource "aws_apigatewayv2_integration" "sqs_integration" {
  api_id                 = aws_apigatewayv2_api.data_ingestor.id
  credentials_arn        = aws_iam_role.sqs_invocation_role.arn
  integration_type       = "AWS_PROXY"
  integration_subtype    = "SQS-SendMessage"
  payload_format_version = "1.0"

  request_parameters = {
    "QueueUrl" = aws_sqs_queue.sqs_bus.id
    "MessageBody" = "$request.body.message" // TODO
  }
}

resource "aws_apigatewayv2_stage" "data_ingestor" {
  name   = var.environment
  api_id = aws_apigatewayv2_api.data_ingestor.id

  deployment_id = aws_apigatewayv2_deployment.data_ingestor.id

  route_settings {
    route_key              = aws_apigatewayv2_route.data_ingestor.route_key
    throttling_rate_limit  = 1
    throttling_burst_limit = 2
  }
}

resource "aws_apigatewayv2_deployment" "data_ingestor" {
  api_id = aws_apigatewayv2_api.data_ingestor.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_apigatewayv2_api.data_ingestor,
      aws_apigatewayv2_authorizer.authorizer,
      aws_apigatewayv2_route.data_ingestor,
      aws_apigatewayv2_integration.sqs_integration,
      aws_iam_role.lambda_invocation_role,
      aws_iam_role.sqs_invocation_role
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }
}
