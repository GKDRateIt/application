package com.github.gkdrateit.service

import com.github.gkdrateit.database.*
import io.javalin.http.Context
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReviewController : CrudApiBase() {
    override val path: String
        get() = "/review"

    private val zoneOffset = ZoneOffset.UTC

    override fun handleCreate(ctx: Context): ApiResponse<String> {
        val param = ctx.paramJsonMap()
        arrayOf(
            "courseId",
//            "userId",
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

        val queryUserId = param["userId"]?.toInt() ?: param["email"]?.let {
            transaction {
                User.find { Users.email eq it }.firstOrNull()?.toModel()
            }?.userId
        } ?: return illegalParamError(listOf("userId", "email"), extraInfo="userId or email is required")

        try {
            transaction {
                Review.new {
                    courseId = param["courseId"]!!.toInt()
                    userId = queryUserId
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
                    commentText = param["commentText"]!!
                    myGrade = param["myGrade"]
                    myMajor = param["myMajor"]?.toInt()
                }
            }
            return success()
        } catch (e: Throwable) {
            return databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context): ApiResponse<List<ReviewModel>> {
        val query = Reviews.selectAll()
        val param = ctx.paramJsonMap()
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

    override fun handleUpdate(ctx: Context): ApiResponse<String> {
        return notImplementedError()
    }

    override fun handleDelete(ctx: Context): ApiResponse<String> {
        val param = ctx.paramJsonMap()
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