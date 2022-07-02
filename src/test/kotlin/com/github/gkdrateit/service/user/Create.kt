package com.github.gkdrateit.service.user


import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.Users
import com.github.gkdrateit.service.ApiResponse
import com.github.gkdrateit.service.ApiServer
import com.github.gkdrateit.service.ResponseStatus
import io.javalin.testtools.JavalinTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class Create {
    private val apiServer = ApiServer()

    @Test
    fun create() = JavalinTest.test(apiServer.app) { server, client ->
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randStr = (1..10)
            .map { allowedChars.random() }
            .joinToString("")
        val nickNameRaw = "test_user_create"
        val nickNameBase64 = Base64.getEncoder().encodeToString(nickNameRaw.toByteArray())
        assertTrue {
            transaction {
                User.find { Users.nickname eq nickNameRaw }.empty()
            }
        }
        val formBody = FormBody.Builder()
            .add("_action", "create")
            .add("email", "test_$randStr@ucas.ac.cn")
            .add("hashedPassword", "123456")
            .add("nickname", nickNameBase64)
            .add("startYear", "2020")
            .add("group", "default")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/user")
            .post(formBody)
            .build()
        client.request(req).use {
            assertEquals(it.code, 200)
            val bodyStr = it.body!!.string()
            val body = Json.decodeFromString<ApiResponse<String>>(bodyStr)
            assertEquals(body.status, ResponseStatus.SUCCESS, bodyStr)
        }
        assertFalse {
            transaction {
                User.find { Users.nickname eq nickNameRaw }.empty()
            }
        }
        transaction {
            Users.deleteWhere {
                Users.nickname eq nickNameRaw
            }
        }
    }
}