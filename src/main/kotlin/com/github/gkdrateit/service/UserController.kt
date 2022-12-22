package com.github.gkdrateit.service

import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.UserModel
import com.github.gkdrateit.database.Users
import com.github.gkdrateit.permission.Admin
import com.github.gkdrateit.permission.Member
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
        arrayOf("email", "hashedPassword", "nickname", "startYear", "verificationCode").forEach { key ->
            if (ctx.formParam(key) == null) {
                return missingParamError(key)
            }
        }

        if (ctx.formParam("verificationCode") != EmailVerificationController.tempCodes[ctx.formParam("email")!!]?.code) {
            return error("Wrong verification code!")
        }

        try {
            val notRegistered = transaction {
                User.find { Users.email eq ctx.formParam("email")!! }.count() == 0L
            }
            val firstUser = transaction {
                Users.selectAll().count() == 0L
            }
            val permission = if (firstUser) {
                Admin
            } else {
                Member
            }
            return if (notRegistered) {
                transaction {
                    User.new {
                        email = ctx.formParam("email")!!
                        hashedPassword = ctx.formParam("hashedPassword")!!
                        nickname = ctx.formParam("nickname")!!
                        startYear = ctx.formParam("startYear")!!
                        group = permission.toString()
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
        val query = Users.selectAll()
        ctx.formParamAsNullable<Int>("userId")?.let {
            query.andWhere { Users.id eq it }
        }
        ctx.formParam("nickname")?.let {
            query.andWhere { Users.nickname like "$it%" }
        }
        ctx.formParam("email")?.let {
            val prefix = it.substringBefore('@')
            val postfix = it.substringAfter('@')
            query.andWhere { Users.email like "$prefix%@$postfix" }
        }
        val totalCount = transaction { query.count() }
        val pagination = getPaginationInfoOrDefault(ctx)
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