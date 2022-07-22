package com.github.gkdrateit.service.review

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Review
import com.github.gkdrateit.database.Reviews
import com.github.gkdrateit.database.User
import io.javalin.testtools.JavalinTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ReviewDelete : TestBase() {
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
        assertFalse {
            transaction {
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
        val postBody = hashMapOf(
            "_action" to "delete",
            "reviewId" to deletedId.toString()
        )
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/review")
            .post(ObjectMapper().writeValueAsString(postBody).toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        client.request(req).use {
            assertEquals(it.code, 200)
            val bodyStr = it.body!!.string()
            assertTrue {
                bodyStr.contains("SUCCESS")
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