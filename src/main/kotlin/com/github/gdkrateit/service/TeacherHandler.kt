package com.github.gdkrateit.service

import com.github.gdkrateit.database.Teacher
import com.github.gdkrateit.database.Teachers
import io.javalin.http.Context
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Base64


class TeacherHandler : CrudApiBase() {
    override val path: String
        get() = "/teacher"

    override fun handleCreate(ctx: Context) {
        if (ctx.formParam("name") == null) {
            ctx.missingParamError("name")
            return
        }

        try {
            base64Decoder.decode(ctx.formParam("email")!!)
        } catch (e: IllegalArgumentException) {
            ctx.base64Error("name")
            return
        }

        try {
            // Q: should I check if it repeats manually?
            transaction {
                Teacher.new {
                    name = ctx.formParam("name")!!
                    email = ctx.formParam("email")
                }
            }
            ctx.success()
        } catch (e: Throwable) {
            ctx.databaseError()
        }
    }

    override fun handleRead(ctx: Context) {
        val name = ctx.formParam("name")
        val email = ctx.formParam("email")
        if (name == null && email == null) {
            ctx.illegalParamError(
                illegalParamNames = listOf("name", "email"),
                extraInfo = "One of them must be non-null."
            )
            return
        }
        val selected = if (name != null) {
            val nameDec = base64Decoder.decode(name).toString()
            Teacher.find {
                Teachers.name eq nameDec
            }
        } else {
            val emailDec = base64Decoder.decode(email!!).toString()
            Teacher.find {
                Teachers.email eq emailDec
            }
        }.toList()
        ctx.successReply(selected)
    }

    override fun handleUpdate(ctx: Context) {
        ctx.notImplementedError()
    }

    override fun handleDelete(ctx: Context) {
        ctx.notImplementedError()
    }
}