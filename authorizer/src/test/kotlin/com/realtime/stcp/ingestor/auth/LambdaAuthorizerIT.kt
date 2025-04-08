package com.realtime.stcp.ingestor.auth

import com.realtime.stcp.ingestor.auth.config.JSONEquals
import com.realtime.stcp.ingestor.auth.config.LocalStackResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@QuarkusTest
@QuarkusTestResource(value = LocalStackResource::class)
class LambdaAuthorizerIT {
    @ParameterizedTest
    @ValueSource(ints = [1, 2])
    fun `should authorize request when x-auth-token matches`(secretNum: Int) {
        val requestBody = buildRequest("secret_value_$secretNum")
        val expected = buildResponse(true)

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .`when`()
            .post()
            .then()
            .statusCode(200)
            .log()
            .ifValidationFails(LogDetail.BODY)
            .body(JSONEquals(expected))
    }

    @Test
    fun `should not authorize when x-auth-token doesn't match`() {
        val requestBody = buildRequest("invalid-token")
        val expected = buildResponse(false)

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .`when`()
            .post()
            .then()
            .statusCode(200)
            .log()
            .ifValidationFails(LogDetail.BODY)
            .body(JSONEquals(expected))
    }

    private fun buildRequest(authToken: String) =
        """
        {
          "identitySource": [
            "x-auth-token"
          ],
          "headers": {
            "x-auth-token": "$authToken"
          },
          "requestContext": {
            "time": "04/Apr/2025:16:33:58 Z"
          }
        }
        """.trimIndent()

    private fun buildResponse(authorized: Boolean) =
        """
        {
            "isAuthorized": $authorized
        }
        """.trimIndent()
}
