package com.github.gkdrateit.service.teacher

import com.github.gkdrateit.createFakeJwt
import com.github.gkdrateit.database.*
import com.github.gkdrateit.service.ApiServer
import io.javalin.testtools.JavalinTest
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TeacherCreateWithoutPermission {
    private val apiServer = ApiServer()

    var testUserId by Delegates.notNull<Int>()
    var testUserEmail = "test_user@test.com"
    val testUserRole = "Member"

    @BeforeAll
    fun setup() {
        TestDbAdapter.setup()
        if (transaction { User.find { Users.email eq testUserEmail }.empty() }) {
            transaction {
                User.new {
                    email = testUserEmail
                    hashedPassword = "???"
                    nickname = "???"
                    startYear = "???"
                    group = "default"
                }
            }
        }
        testUserId = transaction { User.all().first().id.value }
    }

    @Test
    fun create() = JavalinTest.test(apiServer.app) { server, client ->
        val testTeacherName = "ttc"
        assertTrue {
            transaction {
                Teacher.find { Teachers.name eq testTeacherName }.empty()
            }
        }
        val body = FormBody.Builder()
            .add("_action", "create")
            .add("name", testTeacherName)
            .add("email", "ttc_no_perm@ucas.ac.cn")
            .build()
        val jwt = createFakeJwt(testUserId, testUserEmail, testUserRole)
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/teacher")
            .header("Authorization", "Bearer $jwt")
            .post(body)
            .build()
        client.request(req).use {
            assertEquals(it.code, 200)
            val bodyStr = it.body!!.string().lowercase()
            assertTrue {
                bodyStr.contains("fail")
            }
        }
        assertTrue {
            transaction {
                Teacher.find { Teachers.name eq testTeacherName }.empty()
            }
        }
    }
}
