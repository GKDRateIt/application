package com.github.gkdrateit.service

import com.github.gkdrateit.database.Review
import com.github.gkdrateit.database.ReviewModel
import com.github.gkdrateit.database.Reviews
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReviewHandler : CrudApiBase() {
    override val path: String
        get() = "/review"

    private val zoneOffset = ZoneOffset.ofHours(0)

    override fun handleCreate(param: Map<String, String>): ApiResponse<String> {
        arrayOf(
            "courseId",
            "userId",
            "createTime",
            "lastUpdateTime",
            "overallRecommendation",
            "quality",
            "difficulty",
            "workload",
            "commentText"
        ).forEach { key ->
            if (param[key] == null) {
                return missingParamError(key)
            }
        }

        val commentTextDec = try {
            String(base64Decoder.decode(param["commentText"]!!))
        } catch (e: Throwable) {
            return base64Error("commentText")
        }
        val myGradeDec: String? = try {
            param["myGrade"]?.let { String(base64Decoder.decode(it)) }
        } catch (e: Throwable) {
            return base64Error("myGrade")
        }

        try {
            transaction {
                Review.new {
                    courseId = param["courseId"]!!.toInt()
                    userId = param["userId"]!!.toInt()
                    createTime = param["createTime"]!!.toLong().let {
                        LocalDateTime.ofEpochSecond(it, 0, zoneOffset)
                    }
                    lastUpdateTime = param["lastUpdateTime"]!!.toLong().let {
                        LocalDateTime.ofEpochSecond(it, 0, zoneOffset)
                    }
                    overallRecommendation = param["overallRecommendation"]!!.toInt()
                    quality = param["quality"]!!.toInt()
                    difficulty = param["difficulty"]!!.toInt()
                    workload = param["workload"]!!.toInt()
                    commentText = commentTextDec
                    myGrade = myGradeDec
                    myMajor = param["myMajor"]?.toInt()
                }
            }
            return success()
        } catch (e: Throwable) {
            return databaseError(e.message ?: "")
        }
    }

    override fun handleRead(param: Map<String, String>): ApiResponse<List<ReviewModel>> {
        val query = Reviews.selectAll()
        param["courseId"]?.let {
            query.andWhere { Reviews.courseId eq it.toInt() }
        }
        param["userId"]?.let {
            query.andWhere { Reviews.userId eq it.toInt() }
        }
        transaction {
            query.map { Review.wrapRow(it).toModel() }
        }.let {
            return successReply(it)
        }
    }

    override fun handleUpdate(param: Map<String, String>): ApiResponse<String> {
        return notImplementedError()
    }

    override fun handleDelete(param: Map<String, String>): ApiResponse<String> {
        val reviewId = param["reviewId"]?.toInt() ?: return missingParamError("reviewId")
        transaction {
            Review.find { Reviews.id eq reviewId }.empty()
        }.let {
            if (it) {
                return illegalParamError("reviewId", "No such review.")
            }
        }
        transaction {
            Reviews.deleteWhere { Reviews.id eq reviewId }
        }
        return success()
    }
}