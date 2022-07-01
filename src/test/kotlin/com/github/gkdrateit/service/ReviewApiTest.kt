package com.github.gkdrateit.service

import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.User
import io.javalin.testtools.JavalinTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.test.assertEquals

internal class ReviewApiTest {
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
        val tid = transaction { Teacher.all().first().id.value }
        if (transaction { Course.all().empty() }) {
            transaction {
                Course.new {
                    code = "B01GB001Y"
                    codeSeq = "A"
                    name = Base64.getEncoder().encodeToString("随便咯".toByteArray())
                    teacherId = tid
                    semester = "spring"
                    credit = BigDecimal.valueOf(1.5)
                    degree = 0
                }
            }
        }
        if (transaction { User.all().empty() }) {
            transaction {
                User.new {
                    email = "test@ucas.ac.cn"
                    hashedPassword = "123456"
                    nickname = Base64.getEncoder().encodeToString("❤Aerith❤".toByteArray())
                    startYear = "2020"
                    group = "default"
                }
            }
        }
        // TODO: query before create
        val curTime = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0))
        val courseId = transaction { Course.all().first().id.value }
        val userId = transaction { User.all().first().id.value }
        val formBody = FormBody.Builder()
            .add("_action", "create")
            .add("courseId", courseId.toString())
            .add("userId", userId.toString())
            .add("createTime", curTime.toString())
            .add("lastUpdateTime", curTime.toString())
            .add("overallRecommendation", "1")
            .add("quality", "1")
            .add("difficulty", "1")
            .add("workload", "1")
            .add("commentText", Base64.getEncoder().encodeToString("???###???".toByteArray()))
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/review")
            .post(formBody)
            .build()
        client.request(req).use {
            assertEquals(it.code, 200)
            val bodyStr = it.body!!.string()
            val body = Json.decodeFromString<ApiResponse<String>>(bodyStr)
            assertEquals(body.status, ResponseStatus.SUCCESS, bodyStr)
        }
        // TODO: query after create
    }
}