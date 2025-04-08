package com.realtime.stcp.ingestor.auth

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import software.amazon.awssdk.services.ssm.model.GetParameterResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException
import software.amazon.awssdk.services.ssm.model.ParameterType
import java.time.ZonedDateTime

class ParameterStoreClientTest {
    companion object {
        val mockedSsmClient: SsmClient = mock(SsmClient::class.java)

        const val VALID_PARAMETER_ARN =
            "arn:aws:ssm:eu-south-2:000000000000:parameter/local/stcp-realtime-data-ingestor/secrets/hmac-secrets"
        const val NOT_FOUND_PARAMETER_ARN =
            "arn:aws:ssm:eu-south-2:000000000000:parameter/local/stcp-realtime-data-ingestor/secrets/not-found-secrets"
        const val INVALID_PAYLOAD_PARAMETER_ARN =
            "arn:aws:ssm:eu-south-2:000000000000:parameter/local/stcp-realtime-data-ingestor/secrets/invalid-secrets"

        private const val SECRET_VALUE_1 = "secret-value-1"
        private const val SECRET_VALUE_2 = "secret-value-2"
        private const val CREATED_AT_1 = "2025-04-06T16:49:47Z"
        private const val CREATED_AT_2 = "2025-04-07T16:49:47Z"
        private const val VALID_PARAMETER_VALUE = """
            {
                "secret_1": {
                    "secret": "$SECRET_VALUE_1",
                    "createdAt": "$CREATED_AT_1"
                },
                "secret_2": {
                    "secret": "$SECRET_VALUE_2",
                    "createdAt": "$CREATED_AT_2"
                }
            }
        """

        private const val INVALID_PARAMETER_VALUE = """
            {
                "secret_1": {
                    "secret": "$SECRET_VALUE_1",
                    "createdAt": "$CREATED_AT_1"
                }
            }
        """

        private val objectMapper = jacksonObjectMapper().registerModules(JavaTimeModule())

        @JvmStatic
        @BeforeAll
        fun setup() {
            Mockito
                .`when`(
                    mockedSsmClient.getParameter(
                        GetParameterRequest
                            .builder()
                            .name(VALID_PARAMETER_ARN)
                            .withDecryption(true)
                            .build(),
                    ),
                ).thenReturn(
                    GetParameterResponse
                        .builder()
                        .parameter(
                            Parameter
                                .builder()
                                .arn(VALID_PARAMETER_ARN)
                                .type(ParameterType.SECURE_STRING)
                                .value(VALID_PARAMETER_VALUE)
                                .build(),
                        ).build(),
                )

            Mockito
                .`when`(
                    mockedSsmClient.getParameter(
                        GetParameterRequest
                            .builder()
                            .name(INVALID_PAYLOAD_PARAMETER_ARN)
                            .withDecryption(true)
                            .build(),
                    ),
                ).thenReturn(
                    GetParameterResponse
                        .builder()
                        .parameter(
                            Parameter
                                .builder()
                                .arn(INVALID_PAYLOAD_PARAMETER_ARN)
                                .type(ParameterType.SECURE_STRING)
                                .value(INVALID_PARAMETER_VALUE)
                                .build(),
                        ).build(),
                )

            Mockito
                .`when`(
                    mockedSsmClient.getParameter(
                        GetParameterRequest
                            .builder()
                            .name(NOT_FOUND_PARAMETER_ARN)
                            .withDecryption(true)
                            .build(),
                    ),
                ).thenThrow(
                    ParameterNotFoundException.create("random message", null),
                )
        }
    }

    @Test
    fun `should return SecretsParameter with correct secrets when called with correct ARN`() {
        val expected =
            SecretsParameter(
                secret1 =
                    Secret(
                        secret = SECRET_VALUE_1,
                        createdAt = ZonedDateTime.parse(CREATED_AT_1),
                    ),
                secret2 =
                    Secret(
                        secret = SECRET_VALUE_2,
                        createdAt = ZonedDateTime.parse(CREATED_AT_2),
                    ),
            )

        val actual =
            ParameterStoreClient(
                ssmClient = mockedSsmClient,
                objectMapper = objectMapper,
            ).getParameter(VALID_PARAMETER_ARN)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return null when called with invalid ARN`() {
        val actual =
            ParameterStoreClient(
                ssmClient = mockedSsmClient,
                objectMapper = objectMapper,
            ).getParameter(NOT_FOUND_PARAMETER_ARN)

        assertNull(actual)
    }

    @Test
    fun `should return null when parameter's payload is not as expected`() {
        val actual =
            ParameterStoreClient(
                ssmClient = mockedSsmClient,
                objectMapper = objectMapper,
            ).getParameter(INVALID_PAYLOAD_PARAMETER_ARN)
        assertNull(actual)
    }
}
