package com.realtime.stcp.ingestor.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParametersRequest
import software.amazon.awssdk.services.ssm.model.GetParametersResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException
import software.amazon.awssdk.services.ssm.model.ParameterType
import java.time.Instant

class ParameterStoreClientTest {
    companion object {
        val mockedSsmClient: SsmClient = mock(SsmClient::class.java)

        const val VALID_PARAMETER_ARN_1 = "/test/stcp-realtime-data-ingestor/secrets/secret_1"
        const val VALID_PARAMETER_ARN_2 = "/test/stcp-realtime-data-ingestor/secrets/secret_2"
        const val INEXISTENT_PARAMETER_ARN = "/test/stcp-realtime-data-ingestor/secrets/not-found-secrets"

        private const val SECRET_VALUE_1 = "secret-value-1"
        private const val SECRET_VALUE_2 = "secret-value-2"

        @JvmStatic
        @BeforeAll
        fun setup() {
            Mockito
                .`when`(
                    mockedSsmClient.getParameters(
                        GetParametersRequest
                            .builder()
                            .names(setOf(VALID_PARAMETER_ARN_1, VALID_PARAMETER_ARN_2))
                            .withDecryption(true)
                            .build(),
                    ),
                ).thenReturn(
                    GetParametersResponse
                        .builder()
                        .parameters(
                            Parameter
                                .builder()
                                .arn(VALID_PARAMETER_ARN_1)
                                .type(ParameterType.SECURE_STRING)
                                .value(SECRET_VALUE_1)
                                .lastModifiedDate(Instant.EPOCH)
                                .build(),
                            Parameter
                                .builder()
                                .arn(VALID_PARAMETER_ARN_2)
                                .type(ParameterType.SECURE_STRING)
                                .value(SECRET_VALUE_2)
                                .lastModifiedDate(Instant.EPOCH.plusMillis(1))
                                .build(),
                        ).build(),
                )

            Mockito
                .`when`(
                    mockedSsmClient.getParameters(
                        GetParametersRequest
                            .builder()
                            .names(setOf(INEXISTENT_PARAMETER_ARN))
                            .withDecryption(true)
                            .build(),
                    ),
                ).thenThrow(
                    ParameterNotFoundException.create("random message", null),
                )
        }
    }

    @Test
    fun `should return secret list with correct secrets when called with correct ARN`() {
        val expected = setOf(SECRET_VALUE_2, SECRET_VALUE_1)

        val actual =
            ParameterStoreClient(
                ssmClient = mockedSsmClient,
            ).getParameters(setOf(VALID_PARAMETER_ARN_1, VALID_PARAMETER_ARN_2))

        assertEquals(expected, actual)
    }

    @Test
    fun `should return an empty list when called with an invalid path`() {
        val actual =
            ParameterStoreClient(
                ssmClient = mockedSsmClient,
            ).getParameters(setOf(INEXISTENT_PARAMETER_ARN))

        assertTrue(actual.isEmpty())
    }
}
