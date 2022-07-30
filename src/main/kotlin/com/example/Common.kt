package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*


fun Any.toMap(): MutableMap<String,Any>{
    val str = objectMapper.writeValueAsString(this)
    return objectMapper.readValue(str,Map::class.java) as MutableMap<String, Any>
}

suspend fun ApplicationCall.respondJson(data: Any){
    response.header("Content-Type","application/json;charset=utf-8")
    respond(HttpStatusCode.OK,objectMapper.writeValueAsString(data))
}

suspend fun ApplicationCall.respondJson(statusCode: HttpStatusCode, data: Any){
    response.header("Content-Type","application/json;charset=utf-8")
    respond(statusCode,objectMapper.writeValueAsString(data))
}