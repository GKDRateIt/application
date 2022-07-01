package com.github.gkdrateit.service

import io.javalin.testtools.JavalinTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

internal class TeacherApiTest {
    private val apiServer = ApiServer()

    @Test
    fun successCreate() = JavalinTest.test(apiServer.app) { server, client ->
        // TODO: query before create
        val formBody = FormBody.Builder()
            .add("action", "create")
            .add("name", Base64.getEncoder().encodeToString("好老师".toByteArray()))
            .add("email", "test@ucas.ac.cn")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/teacher")
            .post(formBody)
            .build()
        client.request(req).use {
            assertEquals(it.code, 200)
            val bodyStr = it.body!!.string()
            val body = Json.decodeFromString<ApiResponse<String>>(bodyStr)
            assertEquals(body.status, ResponseStatus.SUCCESS, bodyStr)
        }
        // TODO: query after create
    }
}