package com.realtime.stcp.ingestor.auth

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent
import io.quarkus.logging.Log

class LambdaAuthorizer : RequestHandler<APIGatewayV2CustomAuthorizerEvent, Boolean> {
    override fun handleRequest(
        event: APIGatewayV2CustomAuthorizerEvent,
        context: Context,
    ): Boolean {
        Log.info("Event: $event")
        Log.info("Context: $context")

        return true
    }
}
