package com.github.gkdrateit.service

import com.github.gkdrateit.database.User
import io.javalin.http.Context
import org.jetbrains.exposed.sql.transactions.transaction

class UserHandler : CrudApiBase() {
    override val path: String
        get() = "/user"

    override fun handleCreate(ctx: Context) {
        arrayOf("email", "hashedPassword", "nickname", "startYear", "group").forEach { key ->
            if (ctx.formParam(key) == null) {
                ctx.missingParamError(key)
                return
            }
        }

        try {
            base64Decoder.decode(ctx.formParam("nickname"))
        } catch (e: IllegalArgumentException) {
            ctx.base64Error("nickname")
            return
        }

        try {
            transaction {
                User.new {
                    email = ctx.formParam("email")!!
                    hashedPassword = ctx.formParam("hashedPassword")!!
                    nickname = ctx.formParam("nickname")!!
                    startYear = ctx.formParam("startYear")!!
                    group = ctx.formParam("group")!!
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