package com.github.gkdrateit.service.review

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
internal class ReviewRead : TestBase() {

    @Test
    fun read() = JavalinTest.test(apiServer.app) { server, client ->
        val qCourseId = transaction { Course.all().first().id.value }
        val qUserId = transaction { User.all().first().id.value }
        val curTime = LocalDateTime.now()
        if (transaction { Review.find { Reviews.commentText like "测试评论%" }.empty() }) {
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
                    commentText = "测试评论_123"
                    myGrade = null
                    myMajor = 1
                }
            }
        }
        val body = FormBody.Builder()
            .add("_action", "read")
            .add("userId", qUserId.toString())
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/review")
            .post(body)
            .build()
        client.request(req).use {
            val bodyStr = it.body!!.string()
            assertEquals(it.code, 200, bodyStr)
            assertTrue {
                bodyStr.contains("SUCCESS")
            }
            assertTrue {
                bodyStr.contains("测试评论_123")
            }
        }
    }
}