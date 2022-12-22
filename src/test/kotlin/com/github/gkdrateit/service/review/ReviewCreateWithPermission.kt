package com.github.gkdrateit.service.review

import com.github.gkdrateit.createFakeJwt
import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Review
import com.github.gkdrateit.database.Reviews
import com.github.gkdrateit.database.User
import io.javalin.testtools.JavalinTest
import okhttp3.FormBody
import okhttp3.Request
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ReviewCreateWithPermission : TestBase() {
    @Test
    fun create() = JavalinTest.test(apiServer.app) { server, client ->
        val curTime = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0))
        val courseId = transaction { Course.all().first().id.value }
        val userId = transaction { User.all().first().id.value }
        val textRaw = "test_review_create"
        assertTrue {
            transaction {
                Review.find { Reviews.commentText eq textRaw }.empty()
            }
        }

        val jwt = createFakeJwt(testMemberUserId, testMemberUserEmail, testMemberUserRole)
        val body = FormBody.Builder()
            .add("_action", "create")
            .add("courseId", courseId.toString())
            .add("userId", userId.toString())
            .add("createTime", curTime.toString())
            .add("lastUpdateTime", curTime.toString())
            .add("overallRecommendation", "1")
            .add("quality", "1")
            .add("difficulty", "1")
            .add("workload", "1")
            .add("commentText", textRaw)
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/review")
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
                Review.find { Reviews.commentText eq textRaw }.empty()
            }
        }
    }
}