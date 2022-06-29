package com.github.gkdrateit.service

import io.javalin.testtools.JavalinTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneralApiTest {
    private val apiServer = ApiServer()

    @Test
    fun missingActionParam() = JavalinTest.test(apiServer.app) { _, client ->
        val paths = listOf("course", "review", "teacher", "user")
        paths.forEach { path ->
            client.post("/api/${path}").use {
                assertEquals(it.code, 200)
                val body = Json.decodeFromString<ApiResponse<String>>(it.body!!.string())
                assertEquals(body.status, ResponseStatus.FAIL)
                assertTrue {
                    body.detail.contains("Must provide parameter `action`")
                }
            }
        }
    }

    @Test
    fun missingFieldParam() = JavalinTest.test(apiServer.app) { server, client ->
        val paths = listOf("course", "review", "teacher", "user")
        paths.forEach { path ->
            val formBody = FormBody.Builder()
                .add("action", "create")
                .build()
            val req = Request.Builder()
                .url("http://localhost:${server.port()}/api/${path}")
                .post(formBody)
                .build()
            // Do NOT user `client.post` method! It uses wrong serialization schema!
            client.request(req).use {
                assertEquals(it.code, 200)
                val body = Json.decodeFromString<ApiResponse<String>>(it.body!!.string())
                assertEquals(body.status, ResponseStatus.FAIL)
                assertTrue {
                    body.detail.contains("Must provide parameter")
                }
            }
        }
    }
}