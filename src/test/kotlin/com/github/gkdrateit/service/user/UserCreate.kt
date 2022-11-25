package com.github.gkdrateit.service.user


import com.github.gkdrateit.database.TestDbAdapter
import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.Users
import com.github.gkdrateit.service.ApiServer
import com.github.gkdrateit.service.EmailVerificationController
import io.javalin.testtools.JavalinTest
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UserCreate {
    private val apiServer = ApiServer()

    @BeforeAll
    fun setup() {
        TestDbAdapter.setup()
    }

    @Test
    fun create() = JavalinTest.test(apiServer.app) { server, client ->
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randStr = (1..10)
            .map { allowedChars.random() }
            .joinToString("")
        val nickNameRaw = "test_user_create"
        assertTrue {
            transaction {
                User.find { Users.nickname eq nickNameRaw }.empty()
            }
        }
        EmailVerificationController.tempCodes["test_$randStr@ucas.ac.cn"] = EmailVerificationController.Code("111111")
        val body = FormBody.Builder()
            .add("_action", "create")
            .add("email", "test_$randStr@ucas.ac.cn")
            .add("verificationCode", "111111")
            .add("hashedPassword", "123456")
            .add("nickname", nickNameRaw)
            .add("startYear", "2020")
            .add("group", "default")
            .build()

        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/user")
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
                User.find { Users.nickname eq nickNameRaw }.empty()
            }
        }
        transaction {
            Users.deleteWhere {
                nickname eq nickNameRaw
            }
        }
    }
}