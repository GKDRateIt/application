package com.github.gkdrateit.service.course

import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Courses
import com.github.gkdrateit.database.Teacher
import io.javalin.testtools.JavalinTest
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseCreate : TestBase() {
    @Test
    fun create() = JavalinTest.test(apiServer.app) { server, client ->
        val qTeacherId = transaction { Teacher.all().first().id.value }
        val nameRaw = "test_course_create"
        val nameBase64 = Base64.getEncoder().encodeToString(nameRaw.toByteArray())
        assertTrue {
            transaction {
                Course.find { Courses.name eq nameRaw }.empty()
            }
        }
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randStr = (1..9)
            .map { allowedChars.random() }
            .joinToString("")
        val body = FormBody.Builder()
            .add("_action", "create")
            .add("code", randStr)
            .add("codeSeq", "A")
            .add("name", nameBase64)
            .add("teacherId", qTeacherId.toString())
            .add("semester", "spring")
            .add("credit", BigDecimal.valueOf(1.5).toString())
            .add("degree", "0")
            .add("category", "unknown")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/course")
            .post(body)
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
                Course.find { Courses.name eq nameRaw }.empty()
            }
        }
    }
}