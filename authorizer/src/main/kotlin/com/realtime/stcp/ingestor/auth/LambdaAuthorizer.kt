package com.realtime.stcp.ingestor.auth

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.quarkus.logging.Log

class LambdaAuthorizer : RequestHandler<APIGatewayV2CustomAuthorizerEvent, JsonNode> {
    private val mapper = jacksonObjectMapper()

    override fun handleRequest(
        event: APIGatewayV2CustomAuthorizerEvent?,
        context: Context?,
    ): JsonNode {
        Log.info("Event: $event")

        return """
            {
              "isAuthorized": true,
              "context": {
                "userId": "abc-123",
                "role": "admin"
              }
            }

            """.trimIndent()
            .let { mapper.readTree(it) }
    }
}
