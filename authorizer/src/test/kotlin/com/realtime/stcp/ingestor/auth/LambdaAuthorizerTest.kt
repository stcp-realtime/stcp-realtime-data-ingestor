package com.realtime.stcp.ingestor.auth

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LambdaAuthorizerTest {
    val json =
        """{
              "isAuthorized": true,
              "context": {
                "userId": "abc-123",
                "role": "admin"
              }
            }""".let {
            ObjectMapper().readTree(it)
        }

    @Test
    fun testStuff() {
        assertThrows<Exception> {
            LambdaAuthorizer("", ParameterStoreClient()).handleRequest(
                APIGatewayV2CustomAuthorizerEvent(),
                null,
            )
        }
    }
}
