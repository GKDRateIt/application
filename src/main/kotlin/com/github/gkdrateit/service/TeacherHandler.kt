package com.github.gkdrateit.service

import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.Teachers
import io.javalin.http.Context
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


class TeacherHandler : CrudApiBase() {
    override val path: String
        get() = "/teacher"

    override fun handleCreate(ctx: Context) {
        if (ctx.formParam("name") == null) {
            ctx.missingParamError("name")
            return
        }

        val nameDec = try {
            String(base64Decoder.decode(ctx.formParam("name")!!))
        } catch (e: IllegalArgumentException) {
            ctx.base64Error("name")
            return
        }

        try {
            // Q: should I check if it repeats manually?
            transaction {
                Teacher.new {
                    name = nameDec
                    email = ctx.formParam("email")
                }
            }
            ctx.success()
        } catch (e: Throwable) {
            ctx.databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context) {
        val query = Teachers.selectAll()
        ctx.formParam("name")?.let {
            query.andWhere { Teachers.name like "$it%" }
        }
        transaction {
            query.map { Teacher.wrapRow(it).toModel() }
        }.let {
            ctx.successReply(it)
        }
    }

    override fun handleUpdate(ctx: Context) {
        ctx.notImplementedError()
    }

    override fun handleDelete(ctx: Context) {
        ctx.notImplementedError()
    }
}