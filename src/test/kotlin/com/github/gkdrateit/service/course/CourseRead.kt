package com.github.gkdrateit.service.course

import io.javalin.testtools.JavalinTest
import okhttp3.FormBody
import okhttp3.Request
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseRead : TestBase() {
    @Test
    fun read() = JavalinTest.test(apiServer.app) { server, client ->
        val body = FormBody.Builder()
            .add("_action", "read")
            .add("name", "测试")
            .build()

        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/course")
            .post(body)
            .build()
        client.request(req).use {
            val bodyStr = it.body!!.string()
            assertEquals(it.code, 200, bodyStr)
            assertTrue {
                bodyStr.contains("SUCCESS")
            }
            assertTrue {
                bodyStr.contains("测试课程-1") &&
                        bodyStr.contains("测试课程-2")
            }
        }
    }
}