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
import java.time.ZoneId
import java.time.ZonedDateTime

class LambdaAuthorizerTest {
    companion object {
        private val mockedParameterStoreClient: ParameterStoreClient = mock(ParameterStoreClient::class.java)
        private const val VALID_ARN = "valid-parameter-arn"

        private val SECRET_1_DATE: ZonedDateTime = ZonedDateTime.of(2025, 4, 8, 12, 0, 0, 0, ZoneId.systemDefault())

        val authorizedResponse = AuthorizerResponse(true)
        val unauthorizedResponse = AuthorizerResponse(false)

        @JvmStatic
        @BeforeAll
        fun setup() {
            Mockito
                .`when`(
                    mockedParameterStoreClient.getParameter(VALID_ARN),
                ).thenReturn(
                    SecretsParameter(
                        secret1 =
                            Secret(
                                secret = "secret-value-1",
                                createdAt = SECRET_1_DATE,
                            ),
                        secret2 =
                            Secret(
                                secret = "secret-value-2",
                                createdAt = SECRET_1_DATE.plusDays(1),
                            ),
                    ),
                )
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2])
    fun `should return isAuthorized = true when x-auth-token header is present and has a valid secret value`(secretNum: Int) {
        val lambdaAuthorizer = LambdaAuthorizer(VALID_ARN, mockedParameterStoreClient)
        val authorizerEvent = buildAuthorizerEvent("secret-value-$secretNum")
        val actual = lambdaAuthorizer.handleRequest(authorizerEvent, null)
        assertEquals(authorizedResponse, actual)
    }

    @Test
    fun `should return isAuthorized = false when x-auth-token header is not present`() {
        val lambdaAuthorizer = LambdaAuthorizer(VALID_ARN, mockedParameterStoreClient)
        val actual = lambdaAuthorizer.handleRequest(buildAuthorizerEvent(addAuthHeader = false), null)
        assertEquals(unauthorizedResponse, actual)
    }

    @Test
    fun `should return isAuthorized = false when x-auth-token header is present but cannot get parameter`() {
        val lambdaAuthorizer = LambdaAuthorizer("invalid-arn", mockedParameterStoreClient)
        val actual = lambdaAuthorizer.handleRequest(buildAuthorizerEvent(), null)
        assertEquals(unauthorizedResponse, actual)
    }

    @Test
    fun `should return isAuthorized = false when x-auth-token header is present but does not match parameter secrets`() {
        val lambdaAuthorizer = LambdaAuthorizer(VALID_ARN, mockedParameterStoreClient)
        val actual = lambdaAuthorizer.handleRequest(buildAuthorizerEvent(), null)
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
