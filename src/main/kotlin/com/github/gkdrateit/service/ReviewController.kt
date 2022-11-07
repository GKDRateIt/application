package com.github.gkdrateit.service

import com.github.gkdrateit.database.Review
import com.github.gkdrateit.database.Reviews
import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.Users
import io.javalin.http.Context
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.orWhere
import org.jetbrains.exposed.sql.select
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
        } ?: return illegalParamError(listOf("userId", "email"), extraInfo = "userId or email is required")

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

    override fun handleRead(ctx: Context): ApiResponse<out Any> {
        val query = Reviews.select { Reviews.id eq -1 }
        val param = ctx.paramJsonMap()
        param["courseId"]?.let {
            query.orWhere { Reviews.courseId eq it.toInt() }
        }
        param["userId"]?.let {
            query.orWhere { Reviews.userId eq it.toInt() }
        }
        param["email"]?.let {
            query
                .adjustColumnSet { innerJoin(Users, { Reviews.userId }, { Users.id }) }
                .adjustSlice { slice(fields + Users.columns) }
                .orWhere {
                    Users.email eq it
                }
        }

        val totalCount = transaction { query.count() }
        val pagination = getPaginationInfoOrDefault(param)
        query.limit(pagination.limit, pagination.offset)
        transaction {
            query.map { Review.wrapRow(it).let{
                it.toModel(transaction{User.find{Users.id eq it.userId}}?.elementAt(0)?.nickname)}}
        }.let {
            return successReply(it, totalCount, pagination)
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