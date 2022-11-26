package com.github.gkdrateit.service

import com.auth0.jwt.JWT
import com.github.gkdrateit.config.RateItConfig
import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.Users
import io.javalin.http.Context
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

class Login : ApiBase() {
    override val method: HttpMethod
        get() = HttpMethod.POST
    override val path: String
        get() = "login"

    override fun handle(ctx: Context) {
        try {
            val auth = ctx.basicAuthCredentials()!!
            val userEmail = auth.username
            val hashedPassword = auth.password
            val user = transaction {
                User.find {
                    Users.email eq userEmail
                }.firstOrNull()
            }
            if (user == null || user.hashedPassword != hashedPassword) {
                // Wrong password or invalid username
                ctx.status(401)
                ctx.json(authError())
                return
            }
            val jwt = JWT.create()
                .withAudience("GKDRateIt")
                .withIssuer(RateItConfig.jwtIssuer)
                .withClaim("email", userEmail)
                .withClaim("userId", user.id.value)
                .withClaim("role", user.group.toString())
                .withExpiresAt(LocalDateTime.now().plusDays(7).toInstant(ZoneOffset.UTC))
                .sign(RateItConfig.algorithm)!!
            ctx.json(successReply(jwt))
        } catch (e: Throwable) {
            ctx.status(401)
            ctx.json(authError())
        }
    }
}