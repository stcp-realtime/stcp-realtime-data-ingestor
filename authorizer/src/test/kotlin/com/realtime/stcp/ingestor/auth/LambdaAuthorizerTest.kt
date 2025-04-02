package com.realtime.stcp.ingestor.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
        val t = LambdaAuthorizer().handleRequest(null, null)
        assertEquals(json, t)
    }
}
