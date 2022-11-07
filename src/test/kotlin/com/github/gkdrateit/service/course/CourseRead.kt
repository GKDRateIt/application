package com.github.gkdrateit.service.course

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.testtools.JavalinTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseRead : TestBase() {
    @Test
    fun read() = JavalinTest.test(apiServer.app) { server, client ->
        val postBody = hashMapOf(
            "_action" to "read",
            "name" to "测试"
        )

        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/course")
            .post(ObjectMapper().writeValueAsString(postBody).toRequestBody("application/json".toMediaTypeOrNull()))
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