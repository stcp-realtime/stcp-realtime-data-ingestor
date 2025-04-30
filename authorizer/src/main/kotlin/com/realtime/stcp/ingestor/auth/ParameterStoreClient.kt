package com.realtime.stcp.ingestor.auth

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Named
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest
import java.net.URI
import java.time.ZonedDateTime
import java.util.Optional

@ApplicationScoped
class ParameterStoreClient(
    @Named("ssmClient") private val ssmClient: SsmClient,
) {
    fun getParameters(parameterDirPath: String): List<String> =
        runCatching {
            buildParameterRequest(parameterDirPath)
                .let { ssmClient.getParametersByPath(it) }
                ?.parameters()
                ?.sortedByDescending { it.lastModifiedDate() }
                ?.map { it.value() }
                ?: emptyList()
        }.getOrElse {
            Log.error(it)
            emptyList()
        }

    private fun buildParameterRequest(parameterDirPath: String): GetParametersByPathRequest =
        GetParametersByPathRequest
            .builder()
            .path(parameterDirPath)
            .recursive(false)
            .withDecryption(true)
            .build()
}

@ApplicationScoped
class SsmClientProvider(
    @ConfigProperty(name = "stcp.realtime.aws.endpoint-override")
    private val endpointOverride: Optional<String>,
    @ConfigProperty(name = "stcp.realtime.aws.region-override")
    private val regionOverride: Optional<String>,
    @ConfigProperty(name = "quarkus.ssm.aws.credentials.static-provider.access-key-id")
    private val accessId: Optional<String>,
    @ConfigProperty(name = "quarkus.ssm.aws.credentials.static-provider.secret-access-key")
    private val accessKey: Optional<String>,
) {
    @Produces
    @Named("ssmClient")
    fun ssmClient(): SsmClient =
        SsmClient
            .builder()
            .also { builder ->
                endpointOverride.ifPresent { builder.endpointOverride(URI.create(it)) }
                regionOverride.ifPresent { builder.region(Region.of(it)) }
                if (accessId.isPresent && accessKey.isPresent) {
                    builder.credentialsProvider { AwsBasicCredentials.create(accessId.get(), accessKey.get()) }
                }
            }.build()
}

data class SecretsParameter(
    @JsonProperty("secret_1")
    private val secret1: Secret,
    @JsonProperty("secret_2")
    private val secret2: Secret,
) {
    val secrets = listOf(secret1, secret2)
}

data class Secret(
    val secret: String,
    val createdAt: ZonedDateTime,
)
