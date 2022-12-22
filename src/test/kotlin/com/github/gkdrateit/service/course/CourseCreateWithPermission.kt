package com.github.gkdrateit.service.course

import com.github.gkdrateit.createFakeJwt
import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Courses
import com.github.gkdrateit.database.Teacher
import io.javalin.testtools.JavalinTest
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseCreateWithPermission : TestBase() {
    @Test
    fun create() = JavalinTest.test(apiServer.app) { server, client ->
        val qTeacherId = transaction { Teacher.all().first().id.value }
        val testCreateCourseName = "tcc"
        assertTrue {
            transaction {
                Course.find { Courses.name eq testCreateCourseName }.empty()
            }
        }
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randStr = (1..9)
            .map { allowedChars.random() }
            .joinToString("")
        val jwt = createFakeJwt(testUserId, testUserEmail, testUserRole)
        val body = FormBody.Builder()
            .add("_action", "create")
            .add("code", randStr)
            .add("codeSeq", "A")
            .add("name", testCreateCourseName)
            .add("teacherId", qTeacherId.toString())
            .add("semester", "spring")
            .add("credit", (1.5).toString())
            .add("degree", "0")
            .add("category", "unknown")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/course")
            .header("Authorization", "Bearer $jwt")
            .post(body)
            .build()
        client.request(req).use {
            assertEquals(it.code, 200)
            val bodyStr = it.body!!.string().lowercase()
            assertTrue {
                bodyStr.contains("success")
            }
        }
        assertTrue {
            !transaction {
                Course.find { Courses.name eq testCreateCourseName }.empty()
            }
        }
        transaction {
            Courses.deleteWhere {
                name eq testCreateCourseName
            }
        }
    }
}