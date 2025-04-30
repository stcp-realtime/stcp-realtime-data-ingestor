package com.realtime.stcp.ingestor.auth

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent.RequestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import org.mockito.Mockito.mock

class LambdaAuthorizerTest {
    companion object {
        private val mockedParameterStoreClient: ParameterStoreClient = mock(ParameterStoreClient::class.java)
        private const val PARAMETER_ARN_1 = "/test/secrets/secret_1"
        private const val PARAMETER_ARN_2 = "/test/secrets/secret_2"

        val authorizedResponse = AuthorizerResponse(true)
        val unauthorizedResponse = AuthorizerResponse(false)

        @JvmStatic
        @BeforeAll
        fun setup() {
            Mockito
                .`when`(
                    mockedParameterStoreClient.getParameters(setOf(PARAMETER_ARN_1, PARAMETER_ARN_2)),
                ).thenReturn(
                    listOf(
                        "secret-value-1",
                        "secret-value-2",
                    ),
                )
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2])
    fun `should return isAuthorized = true when x-auth-token header is present and has a valid secret value`(secretNum: Int) {
        val lambdaAuthorizer = LambdaAuthorizer(setOf(PARAMETER_ARN_1, PARAMETER_ARN_2), mockedParameterStoreClient)
        val authorizerEvent = buildAuthorizerEvent("secret-value-$secretNum")
        val actual = lambdaAuthorizer.handleRequest(authorizerEvent, null)
        assertEquals(authorizedResponse, actual)
    }

    @Test
    fun `should return isAuthorized = false when x-auth-token header is not present`() {
        val lambdaAuthorizer = LambdaAuthorizer(setOf(PARAMETER_ARN_1, PARAMETER_ARN_2), mockedParameterStoreClient)
        val actual = lambdaAuthorizer.handleRequest(buildAuthorizerEvent(addAuthHeader = false), null)
        assertEquals(unauthorizedResponse, actual)
    }

    @Test
    fun `should return isAuthorized = false when x-auth-token header is present but cannot get parameter`() {
        val lambdaAuthorizer = LambdaAuthorizer(setOf("invalid-arn"), mockedParameterStoreClient)
        val actual = lambdaAuthorizer.handleRequest(buildAuthorizerEvent("secret-value-1"), null)
        assertEquals(unauthorizedResponse, actual)
    }

    @Test
    fun `should return isAuthorized = false when x-auth-token header is present but does not match parameter secrets`() {
        val lambdaAuthorizer = LambdaAuthorizer(setOf(PARAMETER_ARN_1, PARAMETER_ARN_2), mockedParameterStoreClient)
        val actual = lambdaAuthorizer.handleRequest(buildAuthorizerEvent("invalid-parameter-arn"), null)
        assertEquals(unauthorizedResponse, actual)
    }

    private fun buildAuthorizerEvent(
        authHeaderValue: String = "random-secret-value",
        addAuthHeader: Boolean = true,
    ) = APIGatewayV2CustomAuthorizerEvent(
        null,
        null,
        null,
        listOf(LambdaAuthorizer.AUTHORIZATION_HEADER),
        null,
        null,
        null,
        null,
        mutableMapOf<String, String>().apply {
            if (addAuthHeader) {
                put(LambdaAuthorizer.AUTHORIZATION_HEADER, authHeaderValue)
            }
        },
        null,
        RequestContext(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "08/Apr/2025:16:33:58 Z",
            1L,
        ),
        null,
        null,
    )
}
