package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.*
import com.example.model.*
import com.example.service.configureFileRoutes
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.io.File
import kotlin.random.Random

val codeMap = mutableMapOf<String,VerifyCode>()
const val CODE_DURATION = 1000*60*10    //10 min

class VerifyCode(val code: String,val expiredTime: Long){

    val expired: Boolean
        get() = System.currentTimeMillis() > expiredTime

    fun verify(code: String): Boolean{
        return this.code == code && !expired
    }
}

fun Application.configureRouting() {

    routing {

        configureFileRoutes()

        get("/error") {
            throw RuntimeException("error")
        }

        get("/hello"){
            call.respondText("hello")
        }

        post("/token") {
            val body = call.receive<Map<String,String>>()
            val tel = body["username"] ?: ""
            val code = body["code"] ?: ""
            val verifyCode = codeMap[tel]
            if (verifyCode == null || !verifyCode.verify(code)){
                verifyCode?.apply {
                    if(expired){
                        codeMap.remove(tel)
                    }
                }
                call.respond(VERIFY_CODE_ERROR)
                return@post
            }
            codeMap.remove(tel)
            val account = accounts.find { it.tel == tel }!!

            val jwt = JWT.create()
                .withAudience(audience)
                .withClaim("id", account.id)
                .withClaim("tel",account.tel)
                .withIssuer(issuer)
                .sign(Algorithm.HMAC256(secret))
            call.respondJson(mapOf("token" to jwt,"account" to account.toMap().apply {
                remove("users")
            }))
        }

        post("/login") {
            val body = call.receive<Map<String,String>>()
            val type = call.parameters["loginType"] ?: "tel"
            val username = body["username"] ?: ""
            val account = accounts.find {
                (type == "tel") && (it.tel == username) || (type == "email" && it.email == username)
            }
            if(account == null){
                call.respond(NOT_FOUND_USER.toHttpStatusCode(), NOT_FOUND_USER)
                return@post
            }
            val code = Random(System.currentTimeMillis()).nextInt(100000,1000000).toString()
            codeMap[username] = VerifyCode(code,System.currentTimeMillis()+ CODE_DURATION)
            call.respondJson(mapOf("code" to code))
        }

        /**
         * 查询下级文件夹，path参数指定要查询的路径
         * 如果为空则返回对应用户的根目录
         */
        authenticate("jwt") {
            get("/files/{user}/{path...}"){
                val user = call.parameters["user"]!!
                val path = call.parameters.getAll("path")?.joinToString("/","/")
                val userDir = File(rootDir,user)
                if(!userDir.exists()){
                    userDir.mkdir()
                }
                val absUserDirPath = userDir.absolutePath
                val queryDir = File(absUserDirPath+path)
                if(!queryDir.exists()){
                    call.respond(NOT_FOUND_FILE)
                    return@get
                }

                val fileItems = (queryDir.listFiles() ?: arrayOf<File>()).map {
                    FileItem(
                        it.absolutePath.replaceFirst(absUserDirPath,""),
                        it.name,
                        it.length(),
                        it.lastModified(),
                        it.isDirectory,
                        findMimeType(it),
                        if (it.isDirectory) it.list()?.size ?: 0 else 0)
                }
                call.respondJson(fileItems)
            }

            get("/users"){
                val payload = call.principal<JWTPrincipal>()!!
                val id =  payload.getClaim("id",Int::class)
                val data = accounts.find { it.id == id }!!.users
                call.respondJson(data)
            }
        }

        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}
