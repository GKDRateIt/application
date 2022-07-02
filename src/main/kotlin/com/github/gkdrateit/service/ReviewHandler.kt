package com.github.gkdrateit.service

import com.github.gkdrateit.database.Review
import io.javalin.http.Context
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReviewHandler : CrudApiBase() {
    override val path: String
        get() = "/review"

    private val zoneOffset = ZoneOffset.ofHours(0)

    override fun handleCreate(ctx: Context) {
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
            if (ctx.formParam(key) == null) {
                ctx.missingParamError(key)
                return
            }
        }

        val commentTextDec = try {
            String(base64Decoder.decode(ctx.formParam("commentText")!!))
        } catch (e: Throwable) {
            ctx.base64Error("commentText")
            return
        }
        val myGradeDec: String? = try {
            ctx.formParam("myGrade")?.let { String(base64Decoder.decode(it)) }
        } catch (e: Throwable) {
            ctx.base64Error("myGrade")
            return
        }

        try {
            transaction {
                Review.new {
                    courseId = ctx.formParam("courseId")!!.toInt()
                    userId = ctx.formParam("userId")!!.toInt()
                    createTime = ctx.formParam("createTime")!!.toLong().let {
                        LocalDateTime.ofEpochSecond(it, 0, zoneOffset)
                    }
                    lastUpdateTime = ctx.formParam("lastUpdateTime")!!.toLong().let {
                        LocalDateTime.ofEpochSecond(it, 0, zoneOffset)
                    }
                    overallRecommendation = ctx.formParam("overallRecommendation")!!.toInt()
                    quality = ctx.formParam("quality")!!.toInt()
                    difficulty = ctx.formParam("difficulty")!!.toInt()
                    workload = ctx.formParam("workload")!!.toInt()
                    commentText = commentTextDec
                    myGrade = myGradeDec
                    myMajor = ctx.formParam("myMajor")?.toInt()
                }
            }
            ctx.success()
        } catch (e: Throwable) {
            ctx.databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context) {
        ctx.notImplementedError()
    }

    override fun handleUpdate(ctx: Context) {
        ctx.notImplementedError()
    }

    override fun handleDelete(ctx: Context) {
        ctx.notImplementedError()
    }
}