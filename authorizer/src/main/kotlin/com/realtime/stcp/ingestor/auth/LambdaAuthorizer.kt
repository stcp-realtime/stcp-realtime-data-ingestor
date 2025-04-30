package com.realtime.stcp.ingestor.auth

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class LambdaAuthorizer(
    @ConfigProperty(name = "stcp.realtime.data-ingestor.secrets.parameter.directory.path")
    private val secretParametersDirPath: String,
    private val parameterStoreClient: ParameterStoreClient,
) : RequestHandler<APIGatewayV2CustomAuthorizerEvent, AuthorizerResponse> {
    companion object {
        const val AUTHORIZATION_HEADER = "x-auth-token"
    }

    override fun handleRequest(
        event: APIGatewayV2CustomAuthorizerEvent,
        context: Context?,
    ): AuthorizerResponse {
        Log.debug("Event: $event")

        val authToken =
            event.headers[AUTHORIZATION_HEADER]
                ?: let {
                    Log.error("Header $AUTHORIZATION_HEADER not found")
                    return AuthorizerResponse(false)
                }

        val secrets = parameterStoreClient.getParameters(secretParametersDirPath)

        return secrets
            .any { it == authToken }
            .let { AuthorizerResponse(it) }
            .also { Log.info("Authorizer response: $it") }
    }
}

data class AuthorizerResponse(
    val isAuthorized: Boolean,
)
