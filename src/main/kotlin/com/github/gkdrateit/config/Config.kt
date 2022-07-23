package com.github.gkdrateit.config

import com.auth0.jwt.algorithms.Algorithm

object Config {
    val port: Int by lazy {
        // TODO: Read config file or environment variable.
        8080
    }
    // TODO: Replace hard coded secret with a real one
    val algorithm = Algorithm.HMAC256("test_secret_gkd_rate_it")
}