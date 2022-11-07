package com.github.gkdrateit.database

import com.github.gkdrateit.config.Config
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

object DbAdapter {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val db by lazy {
        Database.connect(
            url = Config.databaseURL,
            driver = Config.databaseDriver,
            user = Config.databaseUser,
            password = Config.databasePassword,
        )
    }

    fun connect() {
        logger.info("Connected to ${db.vendor}.")
    }
}