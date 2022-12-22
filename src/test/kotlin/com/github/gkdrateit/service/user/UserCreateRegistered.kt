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
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UserCreateRegistered {
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
        val testUserNickname = "tuc_registered"
        assertTrue {
            transaction {
                User.find { Users.nickname eq testUserNickname }.empty()
            }
        }
        val testUserEmail = "test_$randStr@mails.ucas.ac.cn"
        val testHashedPassword = "123456"
        val testStartYear = "2020"
        val testGroup = "default"
        transaction {
            User.new {
                email = testUserEmail
                hashedPassword = testHashedPassword
                nickname = testUserNickname
                startYear = testStartYear
                group = testGroup
            }
        }
        assertTrue {
            !transaction {
                User.find { Users.nickname eq testUserNickname }.empty()
            }
        }
        EmailVerificationController.tempCodes[testUserEmail] = EmailVerificationController.Code("111111")
        val body = FormBody.Builder()
            .add("_action", "create")
            .add("email", testUserEmail)
            .add("verificationCode", "111111")
            .add("hashedPassword", testHashedPassword)
            .add("nickname", testUserNickname)
            .add("startYear", testStartYear)
            .add("group", testGroup)
            .build()

        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/user")
            .post(body)
            .build()
        client.request(req).use {
            assertEquals(it.code, 200)
            val bodyStr = it.body!!.string().lowercase()
            assertTrue {
                bodyStr.contains("fail")
            }
            assertTrue {
                bodyStr.contains("registered")
            }
        }
    }
}
