package com.example

import com.example.model.Config
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.pathString

const val RootDirName = "cloud_server"

val objectMapper = ObjectMapper().apply {
    registerKotlinModule()
}

lateinit var rootDir: File

val config = loadConfig()
val accounts = config.accounts

fun init(){
    val userDir = File(System.getProperty("user.home"))
    rootDir = File(userDir, RootDirName)
    if(!rootDir.exists() && !rootDir.mkdir()) {
        throw RuntimeException("创建根文件夹失败")
    }
}

fun loadConfig(): Config {
    //加载内置配置文件
    val input = Main::class.java.getResourceAsStream("/config.json")
    return objectMapper.readValue(input, Config::class.java)
}

fun test(f: Runnable){
    f.run()
}



fun main() {
    init()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSerialization()
        configAuthentication()
        configureRouting()
    }.start(wait = true)
}
//仅用来获取AppClassLoader
class Main