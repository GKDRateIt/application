package com.github.gkdrateit.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.gkdrateit.config.Config
import com.github.gkdrateit.database.User
import com.github.gkdrateit.database.Users
import io.javalin.http.Context
import org.jetbrains.exposed.sql.transactions.transaction

class Login : ApiBase() {
    override val method: HttpMethod
        get() = HttpMethod.POST
    override val path: String
        get() = "login"

    override fun handle(ctx: Context) {
        try {
            val auth = ctx.basicAuthCredentials()
            val userEmail = auth.username
            val hashedPassword = auth.password
            transaction {
                // Verify user email and hashed password
                User.find { Users.email eq userEmail }.firstOrNull()?.let {
                    it.hashedPassword == hashedPassword
                } != true
            }.let {
                if (it) {
                    // Wrong password or invalid username
                    ctx.status(401)
                    ctx.json(authError())
                    return
                }
            }
            val jwt = JWT.create()
                .withAudience("GKDRateIt")
                .withIssuer("GKDRateIt")
                .withClaim("email", userEmail)
                .sign(Config.algorithm)!!
            ctx.json(successReply(jwt))
        } catch (e: Throwable) {
            ctx.status(401)
            ctx.json(authError())
        }
    }
}