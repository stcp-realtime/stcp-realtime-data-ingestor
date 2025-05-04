package com.realtime.stcp.ingestor.auth

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Named
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParametersRequest
import java.net.URI
import java.util.Optional

@ApplicationScoped
class ParameterStoreClient(
    @Named("ssmClient") private val ssmClient: SsmClient,
) {
    fun getParameters(parameterArns: Set<String>): Set<String> =
        runCatching {
            buildParameterRequest(parameterArns)
                .let { ssmClient.getParameters(it) }
                ?.parameters()
                ?.map { it.value() }
                ?.toSet()
                ?: emptySet()
        }.getOrElse {
            Log.error(it)
            emptySet()
        }

    private fun buildParameterRequest(arns: Set<String>): GetParametersRequest? =
        GetParametersRequest
            .builder()
            .names(arns)
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
