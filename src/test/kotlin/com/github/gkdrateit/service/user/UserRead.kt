package com.github.gkdrateit.service.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.Users
import com.github.gkdrateit.service.ApiServer
import io.javalin.testtools.JavalinTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class UserRead {
    private val apiServer = ApiServer()

    @Test
    fun read() = JavalinTest.test(apiServer.app) { server, client ->
        if (transaction { User.find { Users.nickname like "测试用户" }.empty() }) {
            val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val randStr = (1..10)
                .map { allowedChars.random() }
                .joinToString("")
            transaction {
                User.new {
                    email = "test_user$randStr@ucas.ac.cn"
                    hashedPassword = "112233"
                    nickname = "测试用户"
                    startYear = "2020"
                    group = "default"
                }
            }
        }
        val postBody = hashMapOf(
            "_action" to "read",
            "nickname" to "测试"
        )
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/user")
            .post(ObjectMapper().writeValueAsString(postBody).toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        client.request(req).use {
            val bodyStr = it.body!!.string()
            assertEquals(it.code, 200, bodyStr)
            assertTrue {
                bodyStr.contains("SUCCESS")
            }
            assertTrue {
                bodyStr.contains("测试用户")
            }
        }
    }
}