package com.example

import com.example.model.FileItem
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.io.File


fun Any.toMap(): MutableMap<String,Any>{
    val str = objectMapper.writeValueAsString(this)
    return objectMapper.readValue(str,Map::class.java) as MutableMap<String, Any>
}

suspend fun ApplicationCall.respondJson(data: Any){
    response.header("Content-Type","application/json;charset=utf-8")
    respondText(objectMapper.writeValueAsString(data))
}

suspend fun ApplicationCall.respondJson(statusCode: HttpStatusCode, data: Any){
    response.header("Content-Type","application/json;charset=utf-8")
    respond(statusCode,objectMapper.writeValueAsString(data))
}

fun findMimeType(file: File): String{
    if (file.isDirectory)
        return "directory"

    val suffix = file.name.substring(file.name.lastIndexOf(".")+1)
    val type = when(suffix.lowercase()){
        "txt","word" -> "text/txt"
        "avi","mp4","mkv" -> "video/${suffix}"
        "jpeg","jpg","png","gif","bmp" -> "image/${suffix}"
        else-> "unknown"
    }
    return type
}

fun File.toFileItem(absUserDirPath: String): FileItem{
    return FileItem(
        absolutePath.replaceFirst(absUserDirPath,""),
        name,
        length(),
        lastModified(),
        isDirectory,
        findMimeType(this),
        if (isDirectory) list()?.size ?: 0 else 0)
}