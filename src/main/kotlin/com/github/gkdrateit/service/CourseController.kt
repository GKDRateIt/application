package com.github.gkdrateit.service

import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.CourseModel
import com.github.gkdrateit.database.Courses
import com.github.gkdrateit.database.Teachers
import com.github.gkdrateit.permission.Permission
import io.javalin.http.Context
import io.javalin.http.formParamAsClass
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class CourseController :
    CrudApiBase() {
    override val path: String
        get() = "/course"


    override fun handleCreate(ctx: Context): ApiResponse<*> {
        arrayOf("code", "name", "teacherId", "semester", "credit", "degree", "category").forEach { key ->
            if (ctx.formParam(key) == null) {
                return missingParamError(key)
            }
        }

        // Verify permission
        val jwt = ctx.javaWebToken() ?: return jwtError()
        if (!jwt.verifyPermission(Permission.COURSE_CREATE)) {
            return permissionError(Permission.COURSE_CREATE)
        }
        val execUser = jwt.claims["userId"]?.asInt() ?: return jwtError()

        try {
            transaction {
                Course.new {
                    code = ctx.formParam("code")!!
                    codeSeq = ctx.formParam("codeSeq")
                    name = ctx.formParam("name")!!
                    teacherId = ctx.formParamAsClass<Int>("teacherId").get()
                    semester = ctx.formParam("semester")!!
                    credit = ctx.formParamAsClass<Double>("credit").get().toBigDecimal()
                    degree = ctx.formParamAsClass<Int>("degree").get()
                    status = 1
                    category = ctx.formParam("category")!!
                    submitUserId = execUser
                }
            }
            return success()
        } catch (e: Throwable) {
            return databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context): ApiResponse<out Any> {
        val result = mutableSetOf<CourseModel>()
        try {
            val query = Courses.select { Courses.status eq 1 }
            ctx.formParamAsNullable<Int>("courseId")?.let {
                query.andWhere { Courses.id eq it }
            }
            ctx.formParam("code")?.let {
                query.andWhere { Courses.code eq it }
            }
            ctx.formParam("codeSeq")?.let {
                query.andWhere { Courses.codeSeq eq it }
            }
            ctx.formParam("name")?.let {
                query.andWhere { Courses.name like "%$it%" }
            }
            ctx.formParamAsNullable<Int>("teacherId")?.let {
                query.andWhere { Courses.teacherId eq it }
            }
            ctx.formParam("semester")?.let {
                query.andWhere { Courses.semester eq it }
            }
            ctx.formParamAsNullable<Double>("credit")?.let {
                query.andWhere { Courses.credit eq it.toBigDecimal() }
            }
            ctx.formParamAsNullable<Int>("degree")?.let {
                query.andWhere { Courses.degree eq it }
            }
            ctx.formParam("category")?.let {
                query.andWhere { Courses.category eq it }
            }
            ctx.formParam("teacherName")?.let {
                query
                    .adjustColumnSet { innerJoin(Teachers, { Courses.id }, { Teachers.id }) }
                    .adjustSlice { slice(fields + Teachers.columns) }
                    .andWhere { Teachers.name like "%$it%" }
            }
            val totalCount = transaction { query.count() }
            val pagination = getPaginationInfoOrDefault(ctx)
            query.limit(pagination.limit, pagination.offset)
            transaction {
                query.map {
                    Course.wrapRow(it).toModel()
                }
            }.let {
                result.addAll(it)
            }
            return successReply(result.toList(), totalCount, pagination)
        } catch (e: Throwable) {
            return databaseError(e.message ?: "")
        }
    }

    override fun handleUpdate(ctx: Context): ApiResponse<*> {
        return notImplementedError()
    }

    override fun handleDelete(ctx: Context): ApiResponse<*> {
        return notImplementedError()
    }
}