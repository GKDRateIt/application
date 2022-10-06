package com.github.gkdrateit.service.course

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Courses
import com.github.gkdrateit.database.Teacher
import io.javalin.testtools.JavalinTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CourseRead : TestBase() {
    @Test
    fun read() = JavalinTest.test(apiServer.app) { server, client ->
        val qTeacherId = transaction { Teacher.all().first().id.value }
        // Create some course entries
        if (transaction { Course.find { Courses.name like "测试课程%" }.empty() }) {
            transaction {
                Course.new {
                    code = "000000000"
                    codeSeq = "A"
                    name = "测试课程-1"
                    teacherId = qTeacherId
                    semester = "spring"
                    credit = BigDecimal.valueOf(1.5)
                    degree = 0
                }
                Course.new {
                    code = "000000001"
                    codeSeq = "B"
                    name = "测试课程-2"
                    teacherId = qTeacherId
                    semester = "spring"
                    credit = BigDecimal.valueOf(1.5)
                    degree = 0
                }
            }
        }
        val postBody = hashMapOf(
            "_action" to "read",
            "name" to "测试"
        )

        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/course")
            .post(ObjectMapper().writeValueAsString(postBody).toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        client.request(req).use {
            val bodyStr = it.body!!.string()
            assertEquals(it.code, 200, bodyStr)
            assertTrue {
                bodyStr.contains("SUCCESS")
            }
            assertTrue {
                bodyStr.contains("测试课程-1") &&
                        bodyStr.contains("测试课程-2")
            }
        }
        // Delete them
        transaction {
            Courses.deleteWhere { Courses.name like "测试课程%" }
        }
        assertTrue {
            transaction { Course.find { Courses.name like "测试课程%" }.empty() }
        }
    }
}