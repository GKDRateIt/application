package com.github.gkdrateit

import com.auth0.jwt.JWT
import com.github.gkdrateit.config.RateItConfig
import java.time.LocalDateTime
import java.time.ZoneOffset

fun createFakeJwt(userId: Int, userEmail: String, userGroup: String): String {
    return JWT.create()
        .withAudience("GKDRateIt")
        .withIssuer(RateItConfig.jwtIssuer)
        .withClaim("email", userEmail)
        .withClaim("userId", userId)
        .withClaim("role", userGroup)
        .withExpiresAt(LocalDateTime.now().plusDays(7).toInstant(ZoneOffset.UTC))
        .sign(RateItConfig.algorithm)!!
}