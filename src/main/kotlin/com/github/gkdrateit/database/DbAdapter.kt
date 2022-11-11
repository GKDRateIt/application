package com.github.gkdrateit.database

import com.github.gkdrateit.config.RateItConfig
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

object DbAdapter {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val db by lazy {
        Database.connect(
            url = RateItConfig.databaseURL,
            driver = RateItConfig.databaseDriver,
            user = RateItConfig.databaseUser,
            password = RateItConfig.databasePassword,
        )
    }

    fun connect() {
        logger.info("Connected to ${db.vendor}.")
    }
}