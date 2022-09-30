package com.github.gkdrateit.service

import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.UserModel
import com.github.gkdrateit.database.Users
import io.javalin.http.Context
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserController : CrudApiBase() {
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
        arrayOf("email", "hashedPassword", "nickname", "startYear", "group", "verificationCode").forEach { key ->
            if (param[key] == null) {
                return missingParamError(key)
            }
        }

        if (param["verificationCode"] != EmailVerificationController.tempCodes[param["email"]!!]?.code) {
            return error("Wrong verification code!")
        }

        try {
            val notRegistered = transaction {
                User.find { Users.email eq param["email"]!! }.count() == 0L
            }
            return if (notRegistered) {
                transaction {
                    User.new {
                        email = param["email"]!!
                        hashedPassword = param["hashedPassword"]!!
                        nickname = param["nickname"]!!
                        startYear = param["startYear"]!!
                        group = param["group"]!!
                    }
                }
                success()
            } else {
                userRegisteredError()
            }
        } catch (e: Throwable) {
            return databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context): ApiResponse<out Any> {
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
        val totalCount = transaction { query.count() }
        val pagination = getPaginationInfoOrDefault(param)
        query.limit(pagination.limit, pagination.offset)
        transaction {
            query.map { User.wrapRow(it).toModel().hidePassword() }
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