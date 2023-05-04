package com.github.gkdrateit.database

import kotlinx.atomicfu.atomic
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

internal object TestDbAdapter {
    private val initialized = atomic(false)

    private fun internalSetup() {
        DbAdapter.connect()
        LoggerFactory.getLogger("").info("Setting up test database.")
        transaction {
            SchemaUtils.create(Courses, Reviews, Teachers, Users)
        }
        transaction {
            val viewStmt = """
                CREATE VIEW AvgRating AS
                (
                    SELECT r_course_id, AVG(r_overall_rec) AS r_avg_overall_rec, AVG(r_rate_quality) AS r_avg_rate_quality,
                        AVG(r_rate_difficulty) AS r_avg_rate_difficulty, AVG(r_rate_workload) AS r_avg_rate_workload
                    FROM reviews
                    GROUP BY r_course_id
                );
            """.trimIndent()
            exec(viewStmt)
        }
    }

    fun setup() {
        if (initialized.compareAndSet(expect = false, update = true)) {
            internalSetup()
        }
    }
}