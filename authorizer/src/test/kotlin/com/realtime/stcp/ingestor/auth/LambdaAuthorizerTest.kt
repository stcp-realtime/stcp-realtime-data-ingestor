package com.realtime.stcp.ingestor.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LambdaAuthorizerTest {
    @Test
    fun testStuff() {
        val t = LambdaAuthorizer().handleRequest(null, null)
        assertEquals(true, t)
    }
}
