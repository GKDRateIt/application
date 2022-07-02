package com.github.gkdrateit.service

import com.github.gkdrateit.database.Course
import io.javalin.http.Context
import org.jetbrains.exposed.sql.transactions.transaction

class CourseHandler : CrudApiBase() {
    override val path: String
        get() = "/course"


    override fun handleCreate(ctx: Context) {
        arrayOf("code", "name", "teacherId", "semester", "credit", "degree").forEach { key ->
            if (ctx.formParam(key) == null) {
                ctx.missingParamError(key)
                return
            }
        }

        val nameDec = try {
            // Do NOT use ByteArray.toString() because it produces wrong format!!!
            String(base64Decoder.decode(ctx.formParam("name")!!))
        } catch (e: Throwable) {
            ctx.base64Error("name")
            return
        }

        try {
            transaction {
                Course.new {
                    code = ctx.formParam("code")!!
                    codeSeq = ctx.formParam("codeSeq")
                    name = nameDec
                    teacherId = ctx.formParam("teacherId")!!.toInt()
                    semester = ctx.formParam("semester")!!
                    credit = ctx.formParam("credit")!!.toBigDecimal()
                    degree = ctx.formParam("degree")!!.toInt()
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