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
internal class ReviewDeleteWithoutPermission : TestBase() {
    @Test
    fun delete() = JavalinTest.test(apiServer.app) { server, client ->
        val qCourseId = transaction { Course.all().first().id.value }
        val qUserId = transaction { User.all().first().id.value }
        val curTime = LocalDateTime.now()
        val testCommentText = "test_review_delete_no_perm"
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
                commentText = testCommentText
            }
        }
        assertTrue {
            !transaction {
                Review.find {
                    Reviews.commentText eq testCommentText
                }.empty()
            }
        }
        val deletedId = transaction {
            Review.find {
                Reviews.commentText eq testCommentText
            }.first().id.value
        }
        // Member permission cannot delete a review;
        val jwt = createFakeJwt(testMemberUserId, testMemberUserEmail, testMemberUserRole)
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
                bodyStr.contains("fail")
            }
            assertTrue {
                bodyStr.contains("permission") || bodyStr.contains("jwt")
            }
        }
        assertTrue {
            !transaction {
                Review.find {
                    Reviews.commentText eq testCommentText
                }.empty()
            }
        }
    }
}
