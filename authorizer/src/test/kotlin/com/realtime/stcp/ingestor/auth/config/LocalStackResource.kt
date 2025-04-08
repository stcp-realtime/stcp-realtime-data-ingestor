package com.realtime.stcp.ingestor.auth.config

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.ParameterType
import software.amazon.awssdk.services.ssm.model.PutParameterRequest
import java.net.URI

class LocalStackResource : QuarkusTestResourceLifecycleManager {
    companion object {
        const val LOCALSTACK_PORT = 4566

        const val SECRET_PARAMETER_FILE_PATH = "data/secrets/secret-parameter.json"
        const val SECRET_PARAMETER_NAME = "/test/stcp-realtime-data-ingestor/secrets/hmac-secrets"
    }

    private val localstack: LocalStackContainer =
        LocalStackContainer(DockerImageName.parse("localstack/localstack:4.3.0"))
            .withExposedPorts(LOCALSTACK_PORT)
            .withServices(LocalStackContainer.Service.SSM)
            .waitingFor(Wait.forHttp("/_localstack/health").forStatusCode(200))
            .apply { logConsumers.add { println(it.utf8String) } }

    private val url: String get() = java.lang.String.format("http://%s:%d", localstack.host, mappedPort)

    private val mappedPort: Int get() = localstack.getMappedPort(LOCALSTACK_PORT)

    private fun initAndGetProperties(): Map<String, String> {
        val parameter =
            javaClass.classLoader.getResource(SECRET_PARAMETER_FILE_PATH)?.readText()
                ?: throw IllegalArgumentException("Unable to read from file $SECRET_PARAMETER_FILE_PATH")

        val createParameterRequest =
            PutParameterRequest
                .builder()
                .name(SECRET_PARAMETER_NAME)
                .value(parameter)
                .type(ParameterType.SECURE_STRING)
                .keyId("alias/aws/ssm")
                .build()

        val ssmClient = createSsmClient()
        ssmClient.putParameter(createParameterRequest)
        val parameterARN = ssmClient.getParameter { it.name(SECRET_PARAMETER_NAME) }.parameter().arn()

        return mapOf(
            "stcp.realtime.aws.endpoint-override" to url,
            "stcp.realtime.aws.region-override" to localstack.region,
            "quarkus.ssm.aws.credentials.static-provider.access-key-id" to localstack.accessKey,
            "quarkus.ssm.aws.credentials.static-provider.secret-access-key" to localstack.secretKey,
            "stcp.realtime.data-ingestor.secrets.parameter.arn" to parameterARN,
        )
    }

    private fun createSsmClient(): SsmClient =
        SsmClient
            .builder()
            .region(Region.of(localstack.region))
            .endpointOverride(URI.create(url))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsSessionCredentials.create(
                        localstack.accessKey,
                        localstack.secretKey,
                        "dummy",
                    ),
                ),
            ).build()

    private fun setAWSLocalstackCredentials() {
        System.setProperty("aws.accessKeyId", localstack.accessKey)
        System.setProperty("aws.secretAccessKey", localstack.secretKey)
        System.setProperty("localstackURL", url)
    }

    override fun start(): MutableMap<String, String> =
        localstack.start().run {
            setAWSLocalstackCredentials()
            initAndGetProperties().toMutableMap()
        }

    override fun stop() {
        localstack.stop()
    }
}
