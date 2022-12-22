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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ReviewDeleteWithPermission : TestBase() {
    @Test
    fun delete() = JavalinTest.test(apiServer.app) { server, client ->
        val qCourseId = transaction { Course.all().first().id.value }
        val qUserId = transaction { User.all().first().id.value }
        val curTime = LocalDateTime.now()
        transaction {
            Review.new {
                courseId = qCourseId
                userId = qUserId
                createTime = curTime
                lastUpdateTime = curTime
                overallRecommendation = 1
                quality = 1
                difficulty = 1
                workload = 1
                commentText = "test_review_delete"
            }
        }
        assertTrue {
            !transaction {
                Review.find {
                    Reviews.commentText eq "test_review_delete"
                }.empty()
            }
        }
        val deletedId = transaction {
            Review.find {
                Reviews.commentText eq "test_review_delete"
            }.first().id.value
        }
        val jwt = createFakeJwt(testAdminUserId, testAdminUserEmail, testAdminUserRole)
        val body = FormBody.Builder()
            .add("_action", "delete")
            .add("reviewId", deletedId.toString())
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
            transaction {
                Review.find {
                    Reviews.commentText eq "test_review_delete"
                }.empty()
            }
        }
    }
}