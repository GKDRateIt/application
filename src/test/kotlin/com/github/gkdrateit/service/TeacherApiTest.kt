package com.github.gkdrateit.service

import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.Teachers
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

internal class TeacherApiTest {
    private val apiServer = ApiServer()

    @Test
    fun successCreate() = JavalinTest.test(apiServer.app) { server, client ->
        val nameBase64 = Base64.getEncoder().encodeToString("test_teacher_create".toByteArray())
        assertTrue {
            transaction {
                Teacher.find { Teachers.name eq nameBase64 }.empty()
            }
        }
        val formBody = FormBody.Builder()
            .add("_action", "create")
            .add("name", nameBase64)
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
        assertFalse {
            transaction {
                Teacher.find { Teachers.name eq nameBase64 }.empty()
            }
        }
        transaction {
            Teachers.deleteWhere {
                Teachers.name eq nameBase64
            }
        }
    }
}