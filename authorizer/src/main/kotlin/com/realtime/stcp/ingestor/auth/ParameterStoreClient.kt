package com.realtime.stcp.ingestor.auth

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.logging.Log
import jakarta.enterprise.context.Dependent
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import kotlin.jvm.optionals.getOrNull

@Dependent
class ParameterStoreClient {
    companion object {
        const val VALUE_FIELD_DESCRIPTOR = "Value"
    }

    private val ssmClient = SsmClient.create()

    fun getParameter(arn: String): ParameterValue? =
        runCatching {
            buildParameterRequest(arn)
                .let { ssmClient.getParameter(it) }
                ?.parameter()
                ?.getValueForField(VALUE_FIELD_DESCRIPTOR, ParameterValue::class.java)
                ?.getOrNull()
        }.getOrElse {
            Log.error(it)
            null
        }

    private fun buildParameterRequest(arn: String): GetParameterRequest =
        GetParameterRequest
            .builder()
            .name(arn)
            .withDecryption(true)
            .build()
}

data class ParameterValue(
    @JsonProperty("key_1")
    private val key1: String,
    @JsonProperty("key_2")
    private val key2: String,
) {
    val keys = listOf(key1, key2)
}
