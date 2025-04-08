package com.realtime.stcp.ingestor.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import java.util.Optional

class SsmClientProviderTest {
    companion object {
        private const val AWS_REGION_PROPERTY = "aws.region"
        private const val DEFAULT_REGION = "eu-central-1"
        private const val ENDPOINT_OVERRIDE = "http://localhost:1001"
    }

    @BeforeEach
    fun setAwsRegion() {
        System.getProperties().setProperty(AWS_REGION_PROPERTY, DEFAULT_REGION)
    }

    @Test
    fun `should return SsmClient with default region and no overridden endpoint`() {
        val ssmClientProvider =
            SsmClientProvider(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())
        val ssmClient = ssmClientProvider.ssmClient()

        val configs = ssmClient.serviceClientConfiguration()
        assertTrue(configs.endpointOverride().isEmpty)
        assertEquals(Region.EU_CENTRAL_1, configs.region())
    }

    @Test
    fun `should return SsmClient with default region and specified overridden endpoint`() {
        val ssmClientProvider =
            SsmClientProvider(
                Optional.of(ENDPOINT_OVERRIDE),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
            )
        val ssmClient = ssmClientProvider.ssmClient()

        val configs = ssmClient.serviceClientConfiguration()
        assertEquals(ENDPOINT_OVERRIDE, configs.endpointOverride().get().toString())
        assertEquals(Region.EU_CENTRAL_1, configs.region())
    }

    @Test
    fun `should return SsmClient with specified region and no overridden endpoint `() {
        val ssmClientProvider =
            SsmClientProvider(
                Optional.empty(),
                Optional.of("eu-south-2"),
                Optional.empty(),
                Optional.empty(),
            )
        val ssmClient = ssmClientProvider.ssmClient()

        val configs = ssmClient.serviceClientConfiguration()
        assertTrue(configs.endpointOverride().isEmpty)
        assertEquals(Region.EU_SOUTH_2, configs.region())
    }

    @Test
    fun `should return SsmClient with specified region and specified overridden endpoint`() {
        val ssmClientProvider =
            SsmClientProvider(
                Optional.of(ENDPOINT_OVERRIDE),
                Optional.of("eu-south-2"),
                Optional.empty(),
                Optional.empty(),
            )
        val ssmClient = ssmClientProvider.ssmClient()

        val configs = ssmClient.serviceClientConfiguration()
        assertEquals(ENDPOINT_OVERRIDE, configs.endpointOverride().get().toString())
        assertEquals(Region.EU_SOUTH_2, configs.region())
    }

    @Test
    fun `should return SsmClient with AwsCredentialsProvider when accessKeyId and accessKeySecret are set`() {
        val ssmClientProvider =
            SsmClientProvider(
                Optional.empty(),
                Optional.empty(),
                Optional.of("accessKeyId"),
                Optional.of("accessKeySecret"),
            )
        val ssmClient = ssmClientProvider.ssmClient()

        val configs = ssmClient.serviceClientConfiguration()
        assertInstanceOf(AwsCredentialsProvider::class.java, configs.credentialsProvider())
        (configs.credentialsProvider() as AwsCredentialsProvider)
            .also {
                assertEquals("accessKeyId", it.resolveCredentials().accessKeyId())
                assertEquals("accessKeySecret", it.resolveCredentials().secretAccessKey())
            }
    }

    @Test
    fun `should return SsmClient with DefaultCredentialsProvider when accessKeyId is not set`() {
        val ssmClientProvider =
            SsmClientProvider(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("accessKeySecret"),
            )
        val ssmClient = ssmClientProvider.ssmClient()

        val configs = ssmClient.serviceClientConfiguration()
        assertInstanceOf(DefaultCredentialsProvider::class.java, configs.credentialsProvider())
    }

    @Test
    fun `should return SsmClient with DefaultCredentialsProvider when accessKeySecret is not set`() {
        val ssmClientProvider =
            SsmClientProvider(
                Optional.empty(),
                Optional.empty(),
                Optional.of("accessKeyId"),
                Optional.empty(),
            )
        val ssmClient = ssmClientProvider.ssmClient()

        val configs = ssmClient.serviceClientConfiguration()
        assertInstanceOf(DefaultCredentialsProvider::class.java, configs.credentialsProvider())
    }

    @Test
    fun `should return SsmClient with DefaultCredentialsProvider when neither accessKeyId nor accessKeySecret are set`() {
        val ssmClientProvider =
            SsmClientProvider(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
            )
        val ssmClient = ssmClientProvider.ssmClient()

        val configs = ssmClient.serviceClientConfiguration()
        assertInstanceOf(DefaultCredentialsProvider::class.java, configs.credentialsProvider())
    }
}
