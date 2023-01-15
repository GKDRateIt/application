package com.github.gkdrateit.service

import com.github.gkdrateit.database.*
import com.github.gkdrateit.permission.Permission
import io.javalin.http.Context
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReviewController : CrudApiBase() {
    override val path: String
        get() = "/review"

    private val zoneOffset = ZoneOffset.UTC

    override fun handleCreate(ctx: Context): ApiResponse<String> {
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
            if (ctx.formParam(key) == null) {
                return missingParamError(key)
            }
        }

        val jwt = ctx.javaWebToken() ?: return jwtError()
        if (!jwt.verifyPermission(Permission.REVIEW_CREATE)) {
            return permissionError(Permission.REVIEW_CREATE)
        }


        val queryUserId = ctx.formParamAsNullable<Int>("userId")
            ?: ctx.formParamAsNullable<String>("email")?.let {
                transaction {
                    User.find { Users.email eq it }.firstOrNull()?.toModel()
                }?.userId
            } ?: return illegalParamError(listOf("userId", "email"), extraInfo = "userId or email is required")

        try {
            transaction {
                Review.new {
                    courseId = ctx.formParamAsNullable("courseId")!!
                    userId = queryUserId
                    createTime = ctx.formParamAsNullable<Long>("createTime")!!.let {
                        LocalDateTime.ofEpochSecond(it, 0, zoneOffset)
                    }
                    lastUpdateTime = ctx.formParamAsNullable<Long>("lastUpdateTime")!!.let {
                        LocalDateTime.ofEpochSecond(it, 0, zoneOffset)
                    }
                    overallRecommendation = ctx.formParamAsNullable("overallRecommendation")!!
                    quality = ctx.formParamAsNullable("quality")!!
                    difficulty = ctx.formParamAsNullable("difficulty")!!
                    workload = ctx.formParamAsNullable("workload")!!
                    commentText = ctx.formParamAsNullable("commentText")!!
                    myGrade = ctx.formParamAsNullable("myGrade")
                    myMajor = ctx.formParamAsNullable("myMajor")
                }
            }
            return success()
        } catch (e: Throwable) {
            return databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context): ApiResponse<out Any> {
        val query = Reviews.selectAll()
        ctx.formParamAsNullable<Int>("courseId")?.let {
            query.andWhere { Reviews.courseId eq it }
        }
        ctx.formParamAsNullable<Int>("userId")?.let {
            query.andWhere { Reviews.userId eq it }
        }
        ctx.formParam("courseCode")?.let {
            query
                .adjustColumnSet { innerJoin(Courses, { Reviews.courseId }, { Courses.id }) }
                .adjustSlice { slice(fields + Courses.columns) }
                .andWhere {
                    Courses.code eq it
                }
            ctx.formParam("courseCodeSeq")?.let {
                query.andWhere {
                    Courses.codeSeq eq it
                }
            }
        }
        ctx.formParam("email")?.let {
            query
                .adjustColumnSet { innerJoin(Users, { Reviews.userId }, { Users.id }) }
                .adjustSlice { slice(fields + Users.columns) }
                .andWhere {
                    Users.email eq it
                }
        }

        val totalCount = transaction { query.count() }
        val pagination = getPaginationInfoOrDefault(ctx)
        query.limit(pagination.limit, pagination.offset)
        transaction {
            query.map { row ->
                Review.wrapRow(row).let {
                    it.toModel(transaction { User.find { Users.id eq it.userId } }.elementAt(0).nickname)
                }
            }
        }.let {
            return successReply(it, totalCount, pagination)
        }
    }

    override fun handleUpdate(ctx: Context): ApiResponse<String> {
        return notImplementedError()
    }

    override fun handleDelete(ctx: Context): ApiResponse<String> {
        // Verify permission
        val jwt = ctx.javaWebToken() ?: return jwtError()
        if (!jwt.verifyPermission(Permission.REVIEW_DELETE)) {
            return permissionError(Permission.REVIEW_DELETE)
        }

        val reviewId = ctx.formParamAsNullable<Int>("reviewId") ?: return missingParamError("reviewId")
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