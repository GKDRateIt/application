package com.github.gkdrateit.config

import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Paths

object Config {
    private const val configFileName = "config.json"
    private val configJson: Map<*, *> by lazy {
        ObjectMapper().readValue(Paths.get(configFileName).toFile(), Map::class.java)
    }

    val port: Int by lazy { configJson["port"]!! as Int }

    // TODO: Replace hard coded secret with a real one
    val algorithm: Algorithm by lazy { Algorithm.HMAC256(configJson["signSecret"]!! as String) }

    val maintainerEmailAddr: String by lazy { configJson["maintainerEmailAddr"]!! as String }
    val maintainerEmailPassword: String by lazy { configJson["maintainerEmailPassword"]!! as String }
    val maintainerEmailSmtpHostName: String by lazy { configJson["maintainerEmailSmtpHostName"]!! as String }
    val maintainerEmailSmtpHostPort: Int by lazy { configJson["maintainerEmailSmtpHostPort"]!! as Int }
}