package com.example

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import com.example.plugins.*
import java.lang.invoke.MethodHandles

class ApplicationTest {
//    @Test
//    fun testRoot() = testApplication {
//        application {
//            configureRouting()
//        }
//        client.get("/").apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("Hello World!", bodyAsText())
//        }
//    }



    open class Test2{
        private fun test() : Int{
            return 1
        }
    }

    @org.junit.Test
    fun methodHandle(){
        val method = Test2::class.java.declaredMethods.find{ it.name == "test"}!!
        method.isAccessible = true
        val handler = MethodHandles.lookup().unreflect(method)
        val a: Int = handler.invoke(Test2()) as Int
        println(a)
    }
}