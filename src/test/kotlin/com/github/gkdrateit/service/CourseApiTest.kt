package com.github.gkdrateit.service

import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Courses
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
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CourseApiTest {
    private val apiServer = ApiServer()

    @Test
    fun successCreate() = JavalinTest.test(apiServer.app) { server, client ->
        if (transaction { Teacher.all().empty() }) {
            transaction {
                Teacher.new {
                    name = "TestTeacher"
                    email = "test_teacher@ucas.ac.cn"
                }
            }
        }
        val teacherId = transaction { Teacher.all().first().id.value }
        val nameBase64 = Base64.getEncoder().encodeToString("test_course_create".toByteArray())
        assertTrue {
            transaction {
                Course.find { Courses.name eq nameBase64 }.empty()
            }
        }
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randStr = (1..9)
            .map { allowedChars.random() }
            .joinToString("")
        val formBody = FormBody.Builder()
            .add("_action", "create")
            .add("code", randStr)
            .add("codeSeq", "A")
            .add("name", nameBase64)
            .add("teacherId", teacherId.toString())
            .add("semester", "spring")
            .add("credit", BigDecimal.valueOf(1.5).toString())
            .add("degree", "0")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/course")
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
                Course.find { Courses.name eq nameBase64 }.empty()
            }
        }
        transaction {
            Courses.deleteWhere {
                Courses.name eq nameBase64
            }
        }
    }
}