package com.github.gkdrateit.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.testtools.JavalinTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneralApiTest {
    private val apiServer = ApiServer()

    @Test
    fun missingActionParam() = JavalinTest.test(apiServer.app) { server, client ->
        val paths = listOf("course", "review", "teacher", "user")
        paths.forEach { path ->
            val req = Request.Builder()
                .url("http://localhost:${server.port()}/api/${path}")
                .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
            client.request(req).use {
                assertEquals(it.code, 200)
                val bodyStr = it.body!!.string()
                assertTrue {
                    bodyStr.contains("FAIL")
                }
                assertTrue {
                    bodyStr.contains("Must provide parameter `_action`")
                }
            }
        }
    }

    @Test
    fun missingFieldParam() = JavalinTest.test(apiServer.app) { server, client ->
        val paths = listOf("course", "review", "teacher", "user")
        paths.forEach { path ->
            val postBody = hashMapOf("_action" to "create")
            val req = Request.Builder()
                .url("http://localhost:${server.port()}/api/${path}")
                .post(ObjectMapper().writeValueAsString(postBody).toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
            // Do NOT user `client.post` method! It uses wrong serialization schema!
            client.request(req).use {
                assertEquals(it.code, 200)
                val bodyStr = it.body!!.string()
                assertTrue {
                    bodyStr.contains("FAIL")
                }
                assertTrue {
                    bodyStr.contains("Must provide parameter")
                }
            }
        }
    }
}