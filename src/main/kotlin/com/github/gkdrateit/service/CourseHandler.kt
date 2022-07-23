package com.github.gkdrateit.service

import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Courses
import io.javalin.http.Context
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class CourseHandler :
    CrudApiBase() {
    override val path: String
        get() = "/course"


    override fun handleCreate(ctx: Context): ApiResponse<*> {
        val param = ctx.paramJsonMap()
        arrayOf("code", "name", "teacherId", "semester", "credit", "degree").forEach { key ->
            if (param[key] == null) {
                return missingParamError(key)
            }
        }

        val nameDec = try {
            // Do NOT use ByteArray.toString() because it produces wrong format!!!
            String(base64Decoder.decode(param["name"]!!))
        } catch (e: Throwable) {
            return base64Error("name")
        }

        try {
            transaction {
                Course.new {
                    code = param["code"]!!
                    codeSeq = param["codeSeq"]
                    name = nameDec
                    teacherId = param["teacherId"]!!.toInt()
                    semester = param["semester"]!!
                    credit = param["credit"]!!.toBigDecimal()
                    degree = param["degree"]!!.toInt()
                }
            }
            return success()
        } catch (e: Throwable) {
            return databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context): ApiResponse<*> {
        val param = ctx.paramJsonMap()
        try {
            val query = Courses.selectAll()
            param["courseId"]?.let {
                query.andWhere { Courses.id eq it.toInt() }
            }
            param["code"]?.let {
                query.andWhere { Courses.code eq it }
            }
            param["codeSeq"]?.let {
                query.andWhere { Courses.codeSeq eq it }
            }
            param["name"]?.let {
                query.andWhere { Courses.name like "$it%" }
            }
            param["teacherId"]?.let {
                query.andWhere { Courses.teacherId eq it.toInt() }
            }
            param["semester"]?.let {
                query.andWhere { Courses.semester eq it }
            }
            param["credit"]?.let {
                query.andWhere { Courses.credit eq it.toBigDecimal() }
            }
            param["degree"]?.let {
                query.andWhere { Courses.degree eq it.toInt() }
            }
            transaction {
                query.map {
                    Course.wrapRow(it).toModel()
                }
            }.let {
                return successReply(it)
            }
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