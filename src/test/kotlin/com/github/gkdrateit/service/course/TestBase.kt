package com.github.gkdrateit.service.course

import com.github.gkdrateit.database.Course
import com.github.gkdrateit.database.Courses
import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.TestDbAdapter
import com.github.gkdrateit.service.ApiServer
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal abstract class TestBase {
    protected val apiServer = ApiServer()

    @BeforeAll
    fun setup() {
        TestDbAdapter.setup()
        prepareTable()
    }

    private fun prepareTable() {
        if (transaction { Teacher.all().empty() }) {
            transaction {
                Teacher.new {
                    name = "TestTeacher"
                    email = "test_teacher@ucas.ac.cn"
                }
            }
        }
        val qTeacherId = transaction { Teacher.all().first().id.value }
        // Create some course entries
        if (transaction { Course.find { Courses.name like "测试课程%" }.empty() }) {
            transaction {
                Course.new {
                    code = "000000000"
                    codeSeq = "A"
                    name = "测试课程-1"
                    teacherId = qTeacherId
                    semester = "spring"
                    credit = BigDecimal.valueOf(1.5)
                    degree = 0
                    status = 1
                }
                Course.new {
                    code = "000000001"
                    codeSeq = "B"
                    name = "测试课程-2"
                    teacherId = qTeacherId
                    semester = "spring"
                    credit = BigDecimal.valueOf(1.5)
                    degree = 0
                    status = 1
                }
            }
        }
    }
}