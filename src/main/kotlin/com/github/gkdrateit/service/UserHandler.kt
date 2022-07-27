package com.github.gkdrateit.service

import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.UserModel
import com.github.gkdrateit.database.Users
import io.javalin.http.Context
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserHandler : CrudApiBase() {
    data class UserModelSimplified(
        val userId: Int,
        val email: String,
        val nickname: String,
        val startYear: String,
        val group: String,
    )

    private fun UserModel.hidePassword(): UserModelSimplified {
        return UserModelSimplified(
            userId = this.userId,
            email = this.email,
            nickname = this.nickname,
            startYear = this.startYear,
            group = this.group,
        )
    }

    override val path: String
        get() = "/user"

    override fun handleCreate(ctx: Context): ApiResponse<String> {
        val param = ctx.paramJsonMap()
        arrayOf("email", "hashedPassword", "nickname", "startYear", "group").forEach { key ->
            if (param[key] == null) {
                return missingParamError(key)
            }
        }

        val nickNameDec = param["nickname"]!!

        return try {
            transaction {
                User.new {
                    email = param["email"]!!
                    hashedPassword = param["hashedPassword"]!!
                    nickname = nickNameDec
                    startYear = param["startYear"]!!
                    group = param["group"]!!
                }
            }
            success()
        } catch (e: Throwable) {
            databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context): ApiResponse<List<UserModelSimplified>> {
        val param = ctx.paramJsonMap()
        val query = Users.selectAll()
        param["userId"]?.let {
            query.andWhere { Users.id eq it.toInt() }
        }
        param["nickname"]?.let {
            query.andWhere { Users.nickname like "$it%" }
        }
        param["email"]?.let {
            val prefix = it.substringBefore('@')
            val postfix = it.substringAfter('@')
            query.andWhere { Users.email like "$prefix%@$postfix" }
        }
        transaction {
            query.map { User.wrapRow(it).toModel().hidePassword() }
        }.let {
            return successReply(it)
        }
    }

    override fun handleUpdate(ctx: Context): ApiResponse<String> {
        return notImplementedError()
    }

    override fun handleDelete(ctx: Context): ApiResponse<String> {
        return notImplementedError()
    }
}