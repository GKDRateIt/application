package com.github.gkdrateit.service.course

import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.service.ApiServer
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal abstract class TestBase {
    protected val apiServer = ApiServer()

    @BeforeAll
    fun prepareTable() {
        if (transaction { Teacher.all().empty() }) {
            transaction {
                Teacher.new {
                    name = "TestTeacher"
                    email = "test_teacher@ucas.ac.cn"
                }
            }
        }
    }
}