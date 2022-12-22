package com.github.gkdrateit.config

import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths

object RateItConfig {
    private var configFileName = run {
        val configName = System.getProperty("CONFIG_FILE")?.let {
            Paths.get(System.getProperty("user.dir"), it).toString()
        } ?: "config.json"
        val configFile = File(configName)
        return@run if (configFile.exists()) {
            configName
        } else {
            throw FileNotFoundException("Must provide $configName or define config file in jvm system property.")
        }
    }
    private val configJson: Map<*, *> = run {
        ObjectMapper().readValue(Paths.get(configFileName).toFile(), Map::class.java)
    }

    val port: Int = run { configJson["port"]!! as Int }

    val algorithm: Algorithm = run { Algorithm.HMAC256(configJson["signSecret"]!! as String) }
    val jwtIssuer = "GKDRateit"

    val maintainerEmailAddr: String = run { configJson["maintainerEmailAddr"]!! as String }
    val maintainerEmailPassword: String = run { configJson["maintainerEmailPassword"]!! as String }
    val maintainerEmailSmtpHostName: String = run { configJson["maintainerEmailSmtpHostName"]!! as String }
    val maintainerEmailSmtpHostPort: Int = run { configJson["maintainerEmailSmtpHostPort"]!! as Int }

    val databaseURL: String = run { configJson["databaseURL"]!! as String }
    val databaseDriver: String = run { configJson["databaseDriver"]!! as String }
    val databaseUser: String = run { configJson["databaseUser"]!! as String }
    val databasePassword: String = run { configJson["databasePassword"]!! as String }
}