package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.model.INVALID_TOKEN
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

const val secret = "com.guojm.coludserver"
const val issuer = "com.guojm"
const val audience = "com.guojm.cloudclient"



fun Application.configAuthentication() {
    install(Authentication) {
        jwt("jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )

            validate{
                if(it.payload.getClaim("username") != null){
                    JWTPrincipal(it.payload)
                } else {
                    null
                }
            }

            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, INVALID_TOKEN)
            }
        }
    }
}