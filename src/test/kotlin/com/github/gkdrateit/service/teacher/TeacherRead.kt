package com.github.gkdrateit.service.teacher

import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.Teachers
import com.github.gkdrateit.database.TestDbAdapter
import com.github.gkdrateit.service.ApiServer
import io.javalin.testtools.JavalinTest
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TeacherRead {
    private val apiServer = ApiServer()

    @BeforeAll
    fun setup() {
        TestDbAdapter.setup()
    }

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
        val body = FormBody.Builder()
            .add("_action", "read")
            .add("name", "测试")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/teacher")
            .post(body)
            .build()
        client.request(req).use {
            val bodyStr = it.body!!.string()
            assertEquals(it.code, 200, bodyStr)
            assertTrue {
                bodyStr.contains("SUCCESS")
            }
            assertTrue {
                bodyStr.contains("测试教师") &&
                        bodyStr.contains("test@ict.ac.cn")
            }
        }
    }
}