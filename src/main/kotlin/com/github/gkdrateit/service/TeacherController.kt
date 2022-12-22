package com.github.gkdrateit.service

import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.Teachers
import com.github.gkdrateit.permission.Permission
import io.javalin.http.Context
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


class TeacherController : CrudApiBase() {
    override val path: String
        get() = "/teacher"

    override fun handleCreate(ctx: Context): ApiResponse<String> {
        if (ctx.formParam("name") == null) {
            return missingParamError("name")
        }

        val jwt = ctx.javaWebToken()
        if (jwt?.verifyPermission(Permission.TEACHER_CREATE) != true) {
            return permissionError()
        }

        return try {
            // Q: should I check if it repeats manually?
            transaction {
                Teacher.new {
                    name = ctx.formParam("name")!!
                    email = ctx.formParam("email")!!
                }
            }
            success()
        } catch (e: Throwable) {
            databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context): ApiResponse<out Any> {
        val query = Teachers.selectAll()
        ctx.formParamAsNullable<Int>("teacherId")?.let {
            query.andWhere { Teachers.id eq it }
        }
        ctx.formParam("name")?.let {
            query.andWhere { Teachers.name like "$it%" }
        }
        val totalCount = transaction { query.count() }
        val pagination = getPaginationInfoOrDefault(ctx)
        query.limit(pagination.limit, pagination.offset)
        transaction {
            query.map { Teacher.wrapRow(it).toModel() }
        }.let {
            return successReply(it, totalCount, pagination)
        }
    }

    override fun handleUpdate(ctx: Context): ApiResponse<String> {
        return notImplementedError()
    }

    override fun handleDelete(ctx: Context): ApiResponse<String> {
        return notImplementedError()
    }
}