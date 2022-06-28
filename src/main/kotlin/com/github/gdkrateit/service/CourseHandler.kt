package com.github.gdkrateit.service

import com.github.gdkrateit.database.Course
import io.javalin.http.Context
import org.jetbrains.exposed.sql.transactions.transaction

class CourseHandler : CrudApiBase() {
    override val path: String
        get() = "/course"


    override fun handleCreate(ctx: Context) {
        arrayOf("id", "code", "name", "teacherId", "semester", "credit", "degree").forEach { key ->
            if (ctx.formParam(key) == null) {
                ctx.missingParamError(key)
                return
            }
        }

        try {
            base64Decoder.decode(ctx.formParam("name"))
        } catch (e: IllegalArgumentException) {
            ctx.base64Error("name")
            return
        }

        try {
            transaction {
                Course.new {
                    code = ctx.formParam("code")!!
                    codeSeq = ctx.formParam("codeSeq")
                    name = ctx.formParam("name")!!
                    teacherId = ctx.formParam("teacherId")!!.toInt()
                    semester = ctx.formParam("semester")!!
                    credit = ctx.formParam("credit")!!.toBigDecimal()
                    degree = ctx.formParam("degree")!!.toInt()
                }
            }
            ctx.success()
        } catch (e: Throwable) {
            ctx.databaseError()
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