package com.realtime.stcp.ingestor.auth

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import software.amazon.awssdk.services.ssm.model.ParameterType
import java.time.Instant

class ParameterStoreClientTest {
    companion object {
        val mockedSsmClient: SsmClient = mock(SsmClient::class.java)

        const val VALID_PARAMETER_DIR_PATH = "/test/stcp-realtime-data-ingestor/secrets/hmac-secrets"
        const val INEXISTENT_PARAMETER_PATH = "/test/stcp-realtime-data-ingestor/secrets/not-found-secrets"
        const val INVALID_PAYLOAD_PARAMETER_ARN = "/test/stcp-realtime-data-ingestor/secrets/invalid-secrets"

        private const val SECRET_VALUE_1 = "secret-value-1"
        private const val SECRET_VALUE_2 = "secret-value-2"

        @JvmStatic
        @BeforeAll
        fun setup() {
            Mockito
                .`when`(
                    mockedSsmClient.getParametersByPath(
                        GetParametersByPathRequest
                            .builder()
                            .path(VALID_PARAMETER_DIR_PATH)
                            .recursive(false)
                            .withDecryption(true)
                            .build(),
                    ),
                ).thenReturn(
                    GetParametersByPathResponse
                        .builder()
                        .parameters(
                            Parameter
                                .builder()
                                .arn(VALID_PARAMETER_DIR_PATH)
                                .type(ParameterType.SECURE_STRING)
                                .value(SECRET_VALUE_1)
                                .lastModifiedDate(Instant.EPOCH)
                                .build(),
                            Parameter
                                .builder()
                                .arn(INVALID_PAYLOAD_PARAMETER_ARN)
                                .type(ParameterType.SECURE_STRING)
                                .value(SECRET_VALUE_2)
                                .lastModifiedDate(Instant.EPOCH.plusMillis(1))
                                .build(),
                        ).build(),
                )

            Mockito
                .`when`(
                    mockedSsmClient.getParametersByPath(
                        GetParametersByPathRequest
                            .builder()
                            .path(INEXISTENT_PARAMETER_PATH)
                            .recursive(false)
                            .withDecryption(true)
                            .build(),
                    ),
                ).thenReturn(
                    GetParametersByPathResponse
                        .builder()
                        .parameters(emptyList<Parameter>())
                        .build(),
                )
        }
    }

    @Test
    fun `should return SecretsParameter with correct secrets when called with correct ARN`() {
        val expected = listOf(SECRET_VALUE_2, SECRET_VALUE_1)

        val actual =
            ParameterStoreClient(
                ssmClient = mockedSsmClient,
            ).getParameters(VALID_PARAMETER_DIR_PATH)

        assertIterableEquals(expected, actual)
    }

    @Test
    fun `should return an empty list when called with an invalid path`() {
        val actual =
            ParameterStoreClient(
                ssmClient = mockedSsmClient,
            ).getParameters(INEXISTENT_PARAMETER_PATH)

        assertTrue(actual.isEmpty())
    }
}
