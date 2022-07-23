package com.github.gkdrateit.service.user


import com.fasterxml.jackson.databind.ObjectMapper
import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.Users
import com.github.gkdrateit.service.ApiServer
import io.javalin.testtools.JavalinTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class UserCreate {
    private val apiServer = ApiServer()

    @Test
    fun create() = JavalinTest.test(apiServer.app) { server, client ->
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randStr = (1..10)
            .map { allowedChars.random() }
            .joinToString("")
        val nickNameRaw = "test_user_create"
        assertTrue {
            transaction {
                User.find { Users.nickname eq nickNameRaw }.empty()
            }
        }
        val postBody = hashMapOf(
            "_action" to "create",
            "email" to "test_$randStr@ucas.ac.cn",
            "hashedPassword" to "123456",
            "nickname" to nickNameRaw,
            "startYear" to "2020",
            "group" to "default"
        )
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/user")
            .post(ObjectMapper().writeValueAsString(postBody).toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        client.request(req).use {
            assertEquals(it.code, 200)
            val bodyStr = it.body!!.string()
            assertTrue {
                bodyStr.contains("SUCCESS")
            }
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