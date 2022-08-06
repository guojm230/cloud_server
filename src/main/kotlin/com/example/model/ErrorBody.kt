package com.example.model

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

@kotlinx.serialization.Serializable
data class ErrorBody(
    val code: Int,
    val msg: String
)

fun ErrorBody.toHttpStatusCode(): HttpStatusCode{
    return HttpStatusCode.fromValue(toHttpCode())
}

suspend fun ApplicationCall.respond(errorBody: ErrorBody){
    respond(errorBody.toHttpStatusCode(),errorBody)
}

enum class ResultCode(val code: Int,val msg: String) {

    NOT_FOUND_USER(404001,"user not found"),

    NOT_FOUND_FILE(404002,"file doesn't exits"),

    UNKNOWN_SERVER_ERROR(500000, "unknown server error")

}

val INVALID_PARAMS = ErrorBody(400000,"invalid params")

val NOT_FOUND_USER = ErrorBody(ResultCode.NOT_FOUND_USER.code,"user doesn't exist")
val INVALID_TOKEN = ErrorBody(401001,"token is invalid or expired")
val VERIFY_CODE_ERROR = ErrorBody(400001,"verify code is error")


val NOT_FOUND_FILE =  ErrorBody(ResultCode.NOT_FOUND_FILE.code,ResultCode.NOT_FOUND_FILE.msg)
val UNKNOWN_SERVER_ERROR = ErrorBody(ResultCode.UNKNOWN_SERVER_ERROR.code,ResultCode.UNKNOWN_SERVER_ERROR.msg)

fun ErrorBody.toHttpCode():Int = code/1000