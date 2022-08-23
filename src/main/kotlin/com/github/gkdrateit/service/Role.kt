package com.github.gkdrateit.service

import io.javalin.core.security.RouteRole

enum class Role : RouteRole {
    ADMIN,
    USER,
    ANYONE,
}