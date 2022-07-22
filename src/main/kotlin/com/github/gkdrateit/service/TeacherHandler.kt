package com.github.gkdrateit.service

import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.TeacherModel
import com.github.gkdrateit.database.Teachers
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


class TeacherHandler : CrudApiBase() {
    override val path: String
        get() = "/teacher"

    override fun handleCreate(param: Map<String, String>): ApiResponse<String> {
        if (param["name"] == null) {
            return missingParamError("name")
        }

        val nameDec = try {
            String(base64Decoder.decode(param["name"]!!))
        } catch (e: IllegalArgumentException) {
            return base64Error("name")
        }

        try {
            // Q: should I check if it repeats manually?
            transaction {
                Teacher.new {
                    name = nameDec
                    email = param["email"]
                }
            }
            return success()
        } catch (e: Throwable) {
            return databaseError(e.message ?: "")
        }
    }

    override fun handleRead(param: Map<String, String>): ApiResponse<List<TeacherModel>> {
        val query = Teachers.selectAll()
        param["name"]?.let {
            query.andWhere { Teachers.name like "$it%" }
        }
        transaction {
            query.map { Teacher.wrapRow(it).toModel() }
        }.let {
            return successReply(it)
        }
    }

    override fun handleUpdate(param: Map<String, String>): ApiResponse<String> {
        return notImplementedError()
    }

    override fun handleDelete(param: Map<String, String>): ApiResponse<String> {
        return notImplementedError()
    }
}