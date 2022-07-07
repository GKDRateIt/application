package com.github.gkdrateit.service.user

import com.github.gkdrateit.database.CourseModel
import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.UserModel
import com.github.gkdrateit.database.Users
import com.github.gkdrateit.service.ApiResponse
import com.github.gkdrateit.service.ApiServer
import com.github.gkdrateit.service.ResponseStatus
import io.javalin.testtools.JavalinTest
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction

internal class Read {
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
        val formBody = FormBody.Builder()
            .add("_action", "read")
            .add("nickname", "测试")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/user")
            .post(formBody)
            .build()
        client.request(req).use {
            val bodyStr = it.body!!.string()
            assertEquals(it.code, 200, bodyStr)
            val body = Json.decodeFromString<ApiResponse<List<UserModel>>>(bodyStr)
            assertEquals(body.status, ResponseStatus.SUCCESS, bodyStr)
            body.data!!.forEach {
                assertTrue {
                    it.nickname.startsWith("测试用户")
                }
            }
        }
    }
}