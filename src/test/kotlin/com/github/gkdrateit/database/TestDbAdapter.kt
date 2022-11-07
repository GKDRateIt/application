package com.github.gkdrateit.database

import kotlinx.atomicfu.atomic
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.logging.Logger

internal object TestDbAdapter {
    private val initialized = atomic(false)

    private fun internalSetup() {
        DbAdapter.connect()
        transaction {
            SchemaUtils.create(Courses)
            SchemaUtils.create(Reviews)
            SchemaUtils.create(Teachers)
            SchemaUtils.create(Users)
        }
    }

    fun setup() {
        Logger.getLogger("TestDb").warning("Setting up test database...")
        if (initialized.compareAndSet(expect = false, update = true)) {
            internalSetup()
        }
    }
}