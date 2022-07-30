package com.example.model

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

@kotlinx.serialization.Serializable
data class ErrorBody<T>(
    val code: Int,
    val data: T? = null
)

fun ErrorBody<*>.toHttpStatusCode(): HttpStatusCode{
    return HttpStatusCode.fromValue(toHttpCode())
}

suspend fun ApplicationCall.respond(errorBody: ErrorBody<Any>){
    respond(errorBody.toHttpStatusCode(),errorBody)
}

enum class ResultCode(val code: Int,val msg: String) {

    NOT_FOUND_USER(404001,"user not found");

}

val INVALID_PARAMS = ErrorBody(400000,"invalid params")

val NOT_FOUND_USER = ErrorBody(ResultCode.NOT_FOUND_USER.code,"user doesn't exist")
val INVALID_TOKEN = ErrorBody(401000,"token is invalid or expired")
val VERIFY_CODE_ERROR = ErrorBody(400001,"verify code is error")

fun ErrorBody<*>.toHttpCode():Int = code/1000