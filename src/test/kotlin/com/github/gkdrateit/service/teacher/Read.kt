package com.github.gkdrateit.service.teacher

import com.github.gkdrateit.database.CourseModel
import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.TeacherModel
import com.github.gkdrateit.database.Teachers
import com.github.gkdrateit.service.ApiResponse
import com.github.gkdrateit.service.ApiServer
import com.github.gkdrateit.service.ResponseStatus
import io.javalin.testtools.JavalinTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class Read {
    private val apiServer = ApiServer()

    @Test
    fun read() = JavalinTest.test(apiServer.app) { server, client ->
        if (transaction { Teacher.find { Teachers.name like "测试教师%" }.empty() }) {
            transaction {
                Teacher.new {
                    name = "测试教师"
                    email = "test@ict.ac.cn"
                }
            }
        }
        val formBody = FormBody.Builder()
            .add("_action", "read")
            .add("name", "测试")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/teacher")
            .post(formBody)
            .build()
        client.request(req).use {
            val bodyStr = it.body!!.string()
            assertEquals(it.code, 200, bodyStr)
            val body = Json.decodeFromString<ApiResponse<List<TeacherModel>>>(bodyStr)
            assertEquals(body.status, ResponseStatus.SUCCESS, bodyStr)
            body.data!!.forEach {
                assertTrue {
                    it.name.startsWith("测试教师")
                }
            }
        }
    }
}