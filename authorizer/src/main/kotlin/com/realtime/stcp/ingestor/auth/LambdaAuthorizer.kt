package com.realtime.stcp.ingestor.auth

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.runBlocking
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class LambdaAuthorizer(
    @ConfigProperty(name = "stcp.realtime.data-ingestor.parameter.arn")
    private val secretParameterArn: String,
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

        val asyncParameterValue = parameterStoreClient.getParameter(secretParameterArn)

        val authHeader = event.headers[AUTHORIZATION_HEADER]
        runBlocking { Log.info("nice! $asyncParameterValue") }
        return AuthorizerResponse(true)
    }
}

data class AuthorizerResponse(
    val isAuthorized: Boolean,
)
