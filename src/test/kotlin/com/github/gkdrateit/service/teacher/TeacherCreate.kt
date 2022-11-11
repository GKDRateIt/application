package com.github.gkdrateit.service.teacher

import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.Teachers
import com.github.gkdrateit.database.TestDbAdapter
import com.github.gkdrateit.service.ApiServer
import io.javalin.testtools.JavalinTest
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TeacherCreate {
    private val apiServer = ApiServer()

    @BeforeAll
    fun setup() {
        TestDbAdapter.setup()
    }

    @Test
    fun create() = JavalinTest.test(apiServer.app) { server, client ->
        val nameRaw = "test_teacher_create"
        val nameBase64 = Base64.getEncoder().encodeToString(nameRaw.toByteArray())
        assertTrue {
            transaction {
                Teacher.find { Teachers.name eq nameBase64 }.empty()
            }
        }
        val body = FormBody.Builder()
            .add("_action", "create")
            .add("name", nameBase64)
            .add("email", "test@ucas.ac.cn")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/teacher")
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
                Teacher.find { Teachers.name eq nameRaw }.empty()
            }
        }
        transaction {
            Teachers.deleteWhere {
                name eq nameRaw
            }
        }
    }
}