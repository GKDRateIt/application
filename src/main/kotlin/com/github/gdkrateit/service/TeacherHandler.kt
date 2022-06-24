package com.github.gdkrateit.service

import com.github.gdkrateit.database.Teacher
import com.github.gdkrateit.database.Teachers
import io.javalin.http.Context


class TeacherHandler : CrudApiBase() {
    override val path: String
        get() = "/teacher"

    override fun handleCreate(ctx: Context) {
        ctx.notImplementedError()
    }

    override fun handleRead(ctx: Context) {
        val name = ctx.formParam("name")
        val email = ctx.formParam("email")
        if (name == null && email == null) {
            ctx.illegalParamError(
                illegalParamNames = listOf("name", "email"),
                extraInfo = "One of them must be non-null."
            )
        }
        val selected = if (name != null) {
            Teacher.find {
                Teachers.name eq name
            }
        } else {
            Teacher.find {
                // The `expr!!` throws exception if `expr` is null.
                // And automatically cast it to the non-null version.
                // But here it won't be null.
                Teachers.email eq email!!
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