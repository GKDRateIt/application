package com.github.gkdrateit.service.review

import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Review
import com.github.gkdrateit.database.Reviews
import com.github.gkdrateit.database.User
import com.github.gkdrateit.service.ApiResponse
import com.github.gkdrateit.service.ResponseStatus
import io.javalin.testtools.JavalinTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class Create : TestBase() {
    @Test
    fun create() = JavalinTest.test(apiServer.app) { server, client ->
        val curTime = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0))
        val courseId = transaction { Course.all().first().id.value }
        val userId = transaction { User.all().first().id.value }
        val textRaw = "test_review_create"
        val textBase64 = Base64.getEncoder().encodeToString(textRaw.toByteArray())
        assertTrue {
            transaction {
                Review.find { Reviews.commentText eq textRaw }.empty()
            }
        }

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
            .add("commentText", textBase64)
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
        assertFalse {
            transaction {
                Review.find { Reviews.commentText eq textRaw }.empty()
            }
        }
        transaction {
            Reviews.deleteWhere {
                Reviews.commentText eq textRaw
            }
        }
    }
}