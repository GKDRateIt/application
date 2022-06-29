package com.github.gkdrateit.database

import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

object DbAdapter {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val db by lazy {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/gkd_rate_it_test",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "112233445566"
        )
    }

    fun connect() {
        logger.info("Connected to ${db.vendor}.")
    }
}