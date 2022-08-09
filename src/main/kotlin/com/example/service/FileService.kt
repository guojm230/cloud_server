package com.example.service

import com.example.model.*
import com.example.respondJson
import com.example.rootDir
import com.example.toFileItem
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*



fun Routing.configureFileRoutes() {

    authenticate("jwt") {

        /**
         * upload file interface
         * 文件存放路径
         * 用户id
         * 文件
         */
        post("/file/upload") {
            val multipart = call.receiveMultipart()

            var partFile: PartData.FileItem? = null
            var userId: Int? = null
            var path: String? = null
            var overwrite: Boolean = false

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "userId" -> userId = try {
                                part.value.toInt()
                            } catch (e: Exception) {
                                null
                            }
                            "path" -> path = part.value
                            "overwrite"-> overwrite = try {
                                part.value.toBoolean()
                            } catch (e: Exception){false}
                        }
                    }
                    is PartData.FileItem -> {
                        partFile = part
                    }
                    else -> {}
                }
            }

            if (userId == null || path == null || partFile == null) {
                call.respond(INVALID_TOKEN.copy(msg = "参数userId、path、partFile必须存在"))
                return@post
            }

            val userDirPath = File(rootDir,userId.toString()).toPath()
            val filePath = Path.of(userDirPath.pathString,path,partFile!!.originalFileName)
            if (filePath.exists() && !overwrite){
                call.respond(ErrorBody(ResultCode.FILE_EXITS_ERROR,"文件已经存在，是否覆盖"))
                return@post
            }
            //TODO 修改为临时文件写入完成后再覆盖
            filePath.deleteIfExists()

            if (!filePath.exists()){
                filePath.createFile()
            }

            withContext(Dispatchers.IO) {
                partFile!!.streamProvider().transferTo(filePath.outputStream())
            }
            call.respondJson(mapOf("success" to true))
        }

        get("/file/item/{user}/{path...}") {
            val user = call.parameters["user"]!!
            val path = call.parameters.getAll("path")?.joinToString("/","/") ?: ""
            val userDir = File(rootDir,user)
            val file = File(userDir,path)

            if (!file.exists()){
                call.respond(ErrorBody(
                    ResultCode.NOT_FOUND,"文件不存在"
                ))
                return@get
            }
            call.respondJson(file.toFileItem(userDir.absolutePath))
        }

        get("/file/{user}/{path...}") {
            val user = call.parameters["user"]!!
            val path = call.parameters.getAll("path")?.joinToString("/","/") ?: ""
            val userDir = File(rootDir,user)
            val downloadFile = File(userDir,path)

            if (!downloadFile.exists()){
                call.respond(NOT_FOUND_FILE)
                return@get
            }

            if (downloadFile.isDirectory) {
                call.respond(INVALID_PARAMS.copy(msg="不能下载文件夹"))
                return@get
            }

            call.respondFile(downloadFile)
        }

        /**
         * 移动文件
         */
        post("/file/{user}/move") {
            val body = call.receive<Map<String, String>>()
            val userDir = File(rootDir, call.parameters["user"]!!)
            val fromPath = body["from"]
            val toPath = body["to"]
            val overwrite = body["overwrite"]?.toBoolean() ?: false

            if (fromPath == null || toPath == null) {
                call.respond(ErrorBody(INVALID_PARAMS.code, "parameter from or to doesn't exits"))
                return@post
            }

            val fromFile = File(userDir, fromPath).toPath()
            val toFile = File(userDir, toPath).toPath()

            if (!fromFile.exists() || !toFile.exists()) {
                call.respond(ErrorBody(ResultCode.NOT_FOUND,"文件不存在"))
                return@post
            }

            if (!toFile.isDirectory()) {
                call.respond(INVALID_PARAMS.copy(msg = "参数to指定的文件必须为文件夹"))
                return@post
            }

            val targetPath = Path.of(toFile.pathString, fromFile.name)
            if (targetPath.exists() && !overwrite){
                call.respond(ErrorBody(ResultCode.FILE_EXITS_ERROR,"目标文件已经存在"))
                return@post
            }

            try {
                fromFile.moveTo(Path.of(toFile.pathString, fromFile.name),overwrite)
            } catch (e: Exception) {
                call.application.environment.log.error("移动文件失败", e)
                call.respondJson(UNKNOWN_SERVER_ERROR)
                return@post
            }
            call.respondJson(mapOf<String, Any>("success" to true))
        }
    }

}