package com.github.gkdrateit.service.review

import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.User
import com.github.gkdrateit.service.ApiServer
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.*

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
        val tid = transaction { Teacher.all().first().id.value }
        if (transaction { Course.all().empty() }) {
            transaction {
                Course.new {
                    code = "B01GB001Y"
                    codeSeq = "A"
                    name = Base64.getEncoder().encodeToString("随便咯".toByteArray())
                    teacherId = tid
                    semester = "spring"
                    credit = BigDecimal.valueOf(1.5)
                    degree = 0
                }
            }
        }
        if (transaction { User.all().empty() }) {
            transaction {
                User.new {
                    email = "test@ucas.ac.cn"
                    hashedPassword = "123456"
                    nickname = Base64.getEncoder().encodeToString("❤Aerith❤".toByteArray())
                    startYear = "2020"
                    group = "default"
                }
            }
        }
    }
}