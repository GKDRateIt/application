package com.github.gdkrateit.service

import io.javalin.http.Context

class CourseHandler : CrudApiBase() {
    override val path: String
        get() = "/course"

    override fun handleCreate(ctx: Context) {
        ctx.notImplementedError()
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