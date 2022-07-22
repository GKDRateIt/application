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
internal class ReviewCreate : TestBase() {
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

        val postBody = hashMapOf(
            "_action" to "create",
            "courseId" to courseId.toString(),
            "userId" to userId.toString(),
            "createTime" to curTime.toString(),
            "lastUpdateTime" to curTime.toString(),
            "overallRecommendation" to "1",
            "quality" to "1",
            "difficulty" to "1",
            "workload" to "1",
            "commentText" to textBase64
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